package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.AudioTrimmer
import com.example.data.local.PlaylistEntity
import com.example.data.repository.MusicRepository
import com.example.model.AudioEffectSettings
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOrder {
  TITLE_ASC, ARTIST_ASC, DURATION_DESC, DATE_DESC
}

private data class EngineState(
  val isPlaying: Boolean,
  val position: Long,
  val duration: Long,
  val sleepTimer: Int?,
  val visualizerBands: List<Float>
)

private data class QueueState(
  val currentTrack: Track?,
  val isShuffle: Boolean,
  val repeatMode: RepeatMode
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = MusicRepository(application)
  val audioEngine = AudioEngine(application)

  val allTracks: StateFlow<List<Track>> = repository.allTracks.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val effectSettings: StateFlow<AudioEffectSettings> = audioEngine.effectSettings

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
  val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

  private val _selectedTab = MutableStateFlow(0) // 0: Library, 1: Player, 2: Bass Lab, 3: Audio Studio, 4: Playlists
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _currentTrack = MutableStateFlow<Track?>(null)
  private val _playbackQueue = MutableStateFlow<List<Track>>(emptyList())
  private val _currentQueueIndex = MutableStateFlow(0)

  private val _isShuffle = MutableStateFlow(false)
  private val _repeatMode = MutableStateFlow(RepeatMode.ALL)

  // Status message / toast feedback
  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // Trimmer state
  private val _trimmerTargetTrack = MutableStateFlow<Track?>(null)
  val trimmerTargetTrack: StateFlow<Track?> = _trimmerTargetTrack.asStateFlow()

  private val _waveformPeaks = MutableStateFlow<List<Float>>(emptyList())
  val waveformPeaks: StateFlow<List<Float>> = _waveformPeaks.asStateFlow()

  private val engineStateFlow = combine(
    audioEngine.isPlaying,
    audioEngine.currentPosition,
    audioEngine.duration,
    audioEngine.sleepTimerMinutes,
    audioEngine.visualizerBands
  ) { isPlaying, position, duration, sleepTimer, visualizerBands ->
    EngineState(isPlaying, position, duration, sleepTimer, visualizerBands)
  }

  private val queueStateFlow = combine(
    _currentTrack,
    _isShuffle,
    _repeatMode
  ) { currentTrack, isShuffle, repeatMode ->
    QueueState(currentTrack, isShuffle, repeatMode)
  }

  val playbackState: StateFlow<PlaybackState> = combine(
    engineStateFlow,
    queueStateFlow
  ) { engine, queue ->
    PlaybackState(
      currentTrack = queue.currentTrack,
      isPlaying = engine.isPlaying,
      currentPositionMs = engine.position,
      durationMs = if (engine.duration > 0) engine.duration else (queue.currentTrack?.durationMs ?: 0L),
      isShuffleEnabled = queue.isShuffle,
      repeatMode = queue.repeatMode,
      sleepTimerMinutesLeft = engine.sleepTimer,
      visualizerAmplitudes = engine.visualizerBands
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = PlaybackState()
  )

  init {
    viewModelScope.launch {
      repository.initializeDefaultsIfEmpty()
    }

    audioEngine.onTrackCompleted = {
      onTrackFinished()
    }
  }

  fun setTab(index: Int) {
    _selectedTab.value = index
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSortOrder(order: SortOrder) {
    _sortOrder.value = order
  }

  fun playTrackFromList(tracks: List<Track>, index: Int) {
    if (tracks.isEmpty() || index !in tracks.indices) return
    _playbackQueue.value = tracks
    _currentQueueIndex.value = index
    val track = tracks[index]
    _currentTrack.value = track
    audioEngine.playTrack(track)
    viewModelScope.launch {
      repository.incrementPlayCount(track.id)
    }
  }

  fun togglePlayPause() {
    if (_currentTrack.value == null && allTracks.value.isNotEmpty()) {
      playTrackFromList(allTracks.value, 0)
    } else {
      audioEngine.togglePlayPause()
    }
  }

  fun nextTrack() {
    val queue = _playbackQueue.value
    if (queue.isEmpty()) return

    val nextIdx = if (_isShuffle.value) {
      (queue.indices).random()
    } else {
      (_currentQueueIndex.value + 1) % queue.size
    }
    _currentQueueIndex.value = nextIdx
    val track = queue[nextIdx]
    _currentTrack.value = track
    audioEngine.playTrack(track)
  }

  fun previousTrack() {
    val queue = _playbackQueue.value
    if (queue.isEmpty()) return

    // If more than 3 seconds in, restart track
    if (audioEngine.currentPosition.value > 3000) {
      audioEngine.seekTo(0)
      return
    }

    val prevIdx = if (_currentQueueIndex.value - 1 < 0) queue.size - 1 else _currentQueueIndex.value - 1
    _currentQueueIndex.value = prevIdx
    val track = queue[prevIdx]
    _currentTrack.value = track
    audioEngine.playTrack(track)
  }

  private fun onTrackFinished() {
    when (_repeatMode.value) {
      RepeatMode.ONE -> {
        _currentTrack.value?.let { audioEngine.playTrack(it, 0) }
      }
      RepeatMode.ALL -> {
        nextTrack()
      }
      RepeatMode.OFF -> {
        if (_currentQueueIndex.value < _playbackQueue.value.size - 1) {
          nextTrack()
        } else {
          audioEngine.pause()
        }
      }
    }
  }

  fun toggleShuffle() {
    _isShuffle.update { !it }
  }

  fun cycleRepeatMode() {
    _repeatMode.update { current ->
      when (current) {
        RepeatMode.ALL -> RepeatMode.ONE
        RepeatMode.ONE -> RepeatMode.OFF
        RepeatMode.OFF -> RepeatMode.ALL
      }
    }
  }

  fun seekTo(progress: Float) {
    val duration = playbackState.value.durationMs
    val targetMs = (progress * duration).toLong()
    audioEngine.seekTo(targetMs)
  }

  fun toggleFavorite(track: Track) {
    viewModelScope.launch {
      repository.toggleFavorite(track.id, track.isFavorite)
      if (_currentTrack.value?.id == track.id) {
        _currentTrack.update { it?.copy(isFavorite = !track.isFavorite) }
      }
    }
  }

  fun deleteTrack(track: Track) {
    viewModelScope.launch {
      if (_currentTrack.value?.id == track.id) {
        audioEngine.stopAndReleasePlayer()
        _currentTrack.value = null
      }
      repository.deleteTrack(track.id)
      showStatus("Track deleted")
    }
  }

  fun scanDeviceStorage() {
    viewModelScope.launch {
      showStatus("Scanning device audio...")
      val count = repository.scanDeviceAudio()
      if (count > 0) {
        showStatus("Imported $count new audio files!")
      } else {
        showStatus("Scan complete. No new files found.")
      }
    }
  }

  fun importAudioUri(uri: Uri) {
    viewModelScope.launch {
      showStatus("Importing audio file...")
      val imported = repository.importAudioUri(uri)
      if (imported != null) {
        showStatus("Imported: ${imported.title}")
      } else {
        showStatus("Failed to import audio file.")
      }
    }
  }

  // Audio Effects
  fun setBassBoost(strength: Int) {
    audioEngine.setBassBoost(strength)
  }

  fun toggleMidnightSubBass(enabled: Boolean) {
    audioEngine.toggleMidnightSubBass(enabled)
  }

  fun setVirtualizer(strength: Int) {
    audioEngine.setVirtualizer(strength)
  }

  fun setEqBandLevel(bandIndex: Short, levelMilliBels: Short) {
    audioEngine.setEqBandLevel(bandIndex, levelMilliBels)
  }

  fun applyPreset(presetName: String) {
    audioEngine.applyPreset(presetName)
  }

  fun setReverb(preset: Short, name: String) {
    audioEngine.setReverb(preset, name)
  }

  fun setPlaybackSpeed(speed: Float) {
    audioEngine.setPlaybackSpeed(speed)
  }

  fun setPlaybackPitch(pitch: Float) {
    audioEngine.setPlaybackPitch(pitch)
  }

  fun setSleepTimer(minutes: Int?) {
    audioEngine.setSleepTimer(minutes)
    if (minutes != null) {
      showStatus("Sleep timer set for $minutes minutes")
    } else {
      showStatus("Sleep timer turned off")
    }
  }

  // Trimmer / Studio
  fun prepareTrimmerForTrack(track: Track) {
    _trimmerTargetTrack.value = track
    viewModelScope.launch {
      val peaks = AudioTrimmer.extractWaveformPeaks(getApplication(), track.uri)
      _waveformPeaks.value = peaks
    }
  }

  fun trimAndSaveAudio(startMs: Long, endMs: Long, clipName: String) {
    val track = _trimmerTargetTrack.value ?: return
    viewModelScope.launch {
      showStatus("Exporting audio clip...")
      val entity = repository.run {
        AudioTrimmer.trimAudio(
          getApplication(),
          track.toEntity(),
          startMs,
          endMs,
          clipName
        )
      }
      if (entity != null) {
        val newId = repository.insertTrack(entity)
        showStatus("Saved '${entity.title}' to library!")
      } else {
        showStatus("Failed to trim audio.")
      }
    }
  }

  // Metadata editor
  fun updateTrackMetadata(trackId: Long, title: String, artist: String, album: String, genre: String?, year: Int?) {
    viewModelScope.launch {
      repository.updateTrackMetadata(trackId, title, artist, album, genre, year)
      if (_currentTrack.value?.id == trackId) {
        _currentTrack.update {
          it?.copy(
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            year = year
          )
        }
      }
      showStatus("Metadata updated successfully")
    }
  }

  // Playlists
  fun createPlaylist(name: String, description: String = "") {
    viewModelScope.launch {
      repository.createPlaylist(name, description)
      showStatus("Playlist '$name' created")
    }
  }

  fun deletePlaylist(playlistId: Long) {
    viewModelScope.launch {
      repository.deletePlaylist(playlistId)
      showStatus("Playlist deleted")
    }
  }

  fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
    viewModelScope.launch {
      repository.addTrackToPlaylist(playlistId, trackId)
      showStatus("Added to playlist")
    }
  }

  fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
    viewModelScope.launch {
      repository.removeTrackFromPlaylist(playlistId, trackId)
      showStatus("Removed from playlist")
    }
  }

  fun getTracksForPlaylist(playlistId: Long) = repository.getTracksForPlaylist(playlistId)

  fun showStatus(message: String) {
    _statusMessage.value = message
  }

  fun clearStatus() {
    _statusMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    audioEngine.stopAndReleasePlayer()
  }
}
