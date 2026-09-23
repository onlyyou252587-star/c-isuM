package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.AudioEffectSettings
import com.example.model.EqBand
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

class AudioEngine(private val context: Context) {

  private val tag = "AudioEngine"
  private val scope = CoroutineScope(Dispatchers.Main + Job())

  private var mediaPlayer: MediaPlayer? = null
  private var equalizer: Equalizer? = null
  private var bassBoost: BassBoost? = null
  private var virtualizer: Virtualizer? = null
  private var presetReverb: PresetReverb? = null

  private var positionUpdateJob: Job? = null
  private var sleepTimerJob: Job? = null

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentPosition = MutableStateFlow(0L)
  val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

  private val _duration = MutableStateFlow(0L)
  val duration: StateFlow<Long> = _duration.asStateFlow()

  private val _visualizerBands = MutableStateFlow(List(20) { 0.05f })
  val visualizerBands: StateFlow<List<Float>> = _visualizerBands.asStateFlow()

  private val _effectSettings = MutableStateFlow(AudioEffectSettings())
  val effectSettings: StateFlow<AudioEffectSettings> = _effectSettings.asStateFlow()

  private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
  val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

  var onTrackCompleted: (() -> Unit)? = null

  // Play a track
  fun playTrack(track: Track, startPositionMs: Long = 0L) {
    stopAndReleasePlayer()

    try {
      val player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )

        // Set data source based on URI or file
        if (track.uri.startsWith("content://") || track.uri.startsWith("android.resource://")) {
          setDataSource(context, Uri.parse(track.uri))
        } else {
          val file = File(track.uri)
          if (file.exists()) {
            setDataSource(file.absolutePath)
          } else {
            setDataSource(context, Uri.parse(track.uri))
          }
        }

        setOnPreparedListener { mp ->
          _duration.value = mp.duration.toLong()
          if (startPositionMs > 0 && startPositionMs < mp.duration) {
            mp.seekTo(startPositionMs.toInt())
          }

          // Initialize audio effects with the player's audio session
          initAudioEffects(mp.audioSessionId)

          // Apply current pitch & speed
          applyPlaybackParams()

          mp.start()
          _isPlaying.value = true
          startPositionTracking()
        }

        setOnCompletionListener {
          _isPlaying.value = false
          _currentPosition.value = 0L
          onTrackCompleted?.invoke()
        }

        setOnErrorListener { _, what, extra ->
          Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
          _isPlaying.value = false
          true
        }

        prepareAsync()
      }
      mediaPlayer = player
    } catch (e: Exception) {
      Log.e(tag, "Failed to initialize playback: ${e.message}", e)
    }
  }

  fun togglePlayPause() {
    mediaPlayer?.let { player ->
      if (player.isPlaying) {
        player.pause()
        _isPlaying.value = false
      } else {
        player.start()
        _isPlaying.value = true
        startPositionTracking()
      }
    }
  }

  fun pause() {
    mediaPlayer?.let { player ->
      if (player.isPlaying) {
        player.pause()
        _isPlaying.value = false
      }
    }
  }

  fun resume() {
    mediaPlayer?.let { player ->
      if (!player.isPlaying) {
        player.start()
        _isPlaying.value = true
        startPositionTracking()
      }
    }
  }

  fun seekTo(positionMs: Long) {
    mediaPlayer?.let { player ->
      val target = positionMs.coerceIn(0, player.duration.toLong()).toInt()
      player.seekTo(target)
      _currentPosition.value = target.toLong()
    }
  }

  // Audio Effects Setup
  private fun initAudioEffects(audioSessionId: Int) {
    releaseEffects()
    val settings = _effectSettings.value

    try {
      equalizer = Equalizer(0, audioSessionId).apply {
        enabled = settings.eqEnabled
        // Query hardware bands
        val numBands = numberOfBands.toInt()
        val minLevel = bandLevelRange[0]
        val maxLevel = bandLevelRange[1]

        val supportedBands = mutableListOf<EqBand>()
        for (i in 0 until numBands) {
          val centerFreq = getCenterFreq(i.toShort()) / 1000 // mHz to Hz
          // Determine existing level from settings if available
          val existingLevel = settings.eqBands.getOrNull(i)?.levelMilliBels ?: 0
          val clampedLevel = existingLevel.coerceIn(minLevel, maxLevel)
          setBandLevel(i.toShort(), clampedLevel)
          supportedBands.add(
            EqBand(
              index = i.toShort(),
              centerFreqHz = centerFreq,
              levelMilliBels = clampedLevel,
              minMilliBels = minLevel,
              maxMilliBels = maxLevel
            )
          )
        }
        _effectSettings.update { it.copy(eqBands = supportedBands) }
      }
    } catch (e: Exception) {
      Log.w(tag, "Equalizer initialization error: ${e.message}")
    }

    try {
      bassBoost = BassBoost(0, audioSessionId).apply {
        enabled = settings.isEnabled
        if (strengthSupported) {
          setStrength(settings.bassBoostStrength.toShort())
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "BassBoost initialization error: ${e.message}")
    }

    try {
      virtualizer = Virtualizer(0, audioSessionId).apply {
        enabled = settings.isEnabled
        if (strengthSupported) {
          setStrength(settings.virtualizerStrength.toShort())
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Virtualizer initialization error: ${e.message}")
    }

    try {
      presetReverb = PresetReverb(0, audioSessionId).apply {
        enabled = settings.reverbPreset != 0.toShort()
        preset = settings.reverbPreset
      }
    } catch (e: Exception) {
      Log.w(tag, "PresetReverb initialization error: ${e.message}")
    }
  }

  fun setBassBoost(strength: Int) {
    val clamped = strength.coerceIn(0, 1000)
    _effectSettings.update { it.copy(bassBoostStrength = clamped) }
    try {
      bassBoost?.let {
        it.enabled = clamped > 0
        if (it.strengthSupported) {
          it.setStrength(clamped.toShort())
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Failed to set bass boost: ${e.message}")
    }
  }

  fun toggleMidnightSubBass(enabled: Boolean) {
    _effectSettings.update { it.copy(isMidnightSubBass = enabled) }
    // If midnight sub bass is enabled, boost the sub-bass EQ band to max and elevate strength
    if (enabled) {
      setEqBandLevel(0, 1200) // +12dB on sub band
      setBassBoost(1000)
    }
  }

  fun setVirtualizer(strength: Int) {
    val clamped = strength.coerceIn(0, 1000)
    _effectSettings.update { it.copy(virtualizerStrength = clamped) }
    try {
      virtualizer?.let {
        it.enabled = clamped > 0
        if (it.strengthSupported) {
          it.setStrength(clamped.toShort())
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Failed to set virtualizer: ${e.message}")
    }
  }

  fun setEqBandLevel(bandIndex: Short, levelMilliBels: Short) {
    val currentBands = _effectSettings.value.eqBands.toMutableList()
    val band = currentBands.find { it.index == bandIndex } ?: return
    val clamped = levelMilliBels.coerceIn(band.minMilliBels, band.maxMilliBels)

    val updatedBand = band.copy(levelMilliBels = clamped)
    val idx = currentBands.indexOfFirst { it.index == bandIndex }
    currentBands[idx] = updatedBand

    _effectSettings.update {
      it.copy(
        eqBands = currentBands,
        currentPresetName = "Custom"
      )
    }

    try {
      equalizer?.setBandLevel(bandIndex, clamped)
    } catch (e: Exception) {
      Log.w(tag, "Failed to set EQ band: ${e.message}")
    }
  }

  fun applyPreset(presetName: String) {
    val bands = _effectSettings.value.eqBands
    if (bands.isEmpty()) return

    val newLevels: List<Short> = when (presetName) {
      "Midnight Bass Signature" -> listOf(1200, 800, -200, 300, 600)
      "Sub Overdrive" -> listOf(1500, 1000, -300, 0, 400)
      "Club & EDM" -> listOf(900, 500, -200, 600, 900)
      "Rock Punch" -> listOf(600, 400, 200, 500, 700)
      "Hip-Hop 808" -> listOf(1100, 700, 0, 300, 500)
      "Vocal Clarity" -> listOf(-300, 0, 500, 800, 600)
      "Acoustic / Jazz" -> listOf(300, 200, 100, 300, 400)
      "Flat" -> listOf(0, 0, 0, 0, 0)
      else -> listOf(0, 0, 0, 0, 0)
    }

    val updatedBands = bands.mapIndexed { i, band ->
      val targetLevel = (newLevels.getOrNull(i) ?: 0).coerceIn(band.minMilliBels, band.maxMilliBels)
      try {
        equalizer?.setBandLevel(band.index, targetLevel)
      } catch (e: Exception) {
        Log.w(tag, "Preset band error: ${e.message}")
      }
      band.copy(levelMilliBels = targetLevel)
    }

    val bassStrength = when (presetName) {
      "Midnight Bass Signature" -> 850
      "Sub Overdrive" -> 1000
      "Club & EDM" -> 750
      "Hip-Hop 808" -> 900
      "Rock Punch" -> 600
      else -> 400
    }
    setBassBoost(bassStrength)

    _effectSettings.update {
      it.copy(
        eqBands = updatedBands,
        currentPresetName = presetName,
        bassBoostStrength = bassStrength
      )
    }
  }

  fun setReverb(preset: Short, name: String) {
    _effectSettings.update {
      it.copy(
        reverbPreset = preset,
        reverbPresetName = name
      )
    }
    try {
      presetReverb?.let {
        it.enabled = preset != 0.toShort()
        it.preset = preset
      }
    } catch (e: Exception) {
      Log.w(tag, "Failed to set reverb: ${e.message}")
    }
  }

  fun setPlaybackSpeed(speed: Float) {
    val clamped = speed.coerceIn(0.5f, 2.0f)
    _effectSettings.update { it.copy(playbackSpeed = clamped) }
    applyPlaybackParams()
  }

  fun setPlaybackPitch(pitch: Float) {
    val clamped = pitch.coerceIn(0.5f, 2.0f)
    _effectSettings.update { it.copy(playbackPitch = clamped) }
    applyPlaybackParams()
  }

  private fun applyPlaybackParams() {
    mediaPlayer?.let { player ->
      try {
        val speed = _effectSettings.value.playbackSpeed
        val pitch = _effectSettings.value.playbackPitch
        val params = player.playbackParams ?: PlaybackParams()
        params.speed = speed
        params.pitch = pitch
        player.playbackParams = params
      } catch (e: Exception) {
        Log.w(tag, "Failed to apply PlaybackParams: ${e.message}")
      }
    }
  }

  fun setSleepTimer(minutes: Int?) {
    sleepTimerJob?.cancel()
    _sleepTimerMinutes.value = minutes

    if (minutes != null && minutes > 0) {
      sleepTimerJob = scope.launch {
        var remaining = minutes
        while (remaining > 0 && isActive) {
          delay(60_000L)
          remaining -= 1
          _sleepTimerMinutes.value = remaining
        }
        pause()
        _sleepTimerMinutes.value = null
      }
    }
  }

  // Periodic position and visualizer animation loop
  private fun startPositionTracking() {
    positionUpdateJob?.cancel()
    positionUpdateJob = scope.launch {
      var wavePhase = 0.0
      while (isActive && _isPlaying.value) {
        mediaPlayer?.let { player ->
          if (player.isPlaying) {
            _currentPosition.value = player.currentPosition.toLong()

            // Dynamic live spectrum simulation based on current playback & bass boost
            wavePhase += 0.2
            val bassBoostFactor = _effectSettings.value.bassBoostStrength / 1000f
            val subFactor = if (_effectSettings.value.isMidnightSubBass) 0.35f else 0f

            val amplitudes = (0 until 20).map { band ->
              val freqOffset = band * 0.4
              val rawSine = abs(sin(wavePhase * 1.5 + freqOffset) * sin(wavePhase * 0.8 + freqOffset * 0.5))
              // Bass bands (0-5) are amplified when bass boost is engaged
              val bassWeight = if (band < 6) 0.5f + (bassBoostFactor * 0.5f) + subFactor else 0.4f
              val amp = (rawSine * bassWeight + 0.08f).toFloat().coerceIn(0.05f, 1.0f)
              amp
            }
            _visualizerBands.value = amplitudes
          }
        }
        delay(60L) // 16fps smooth UI visualizer update
      }
    }
  }

  private fun releaseEffects() {
    try {
      equalizer?.release()
    } catch (_: Exception) {}
    try {
      bassBoost?.release()
    } catch (_: Exception) {}
    try {
      virtualizer?.release()
    } catch (_: Exception) {}
    try {
      presetReverb?.release()
    } catch (_: Exception) {}
    equalizer = null
    bassBoost = null
    virtualizer = null
    presetReverb = null
  }

  fun stopAndReleasePlayer() {
    positionUpdateJob?.cancel()
    _isPlaying.value = false
    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
    releaseEffects()
  }
}
