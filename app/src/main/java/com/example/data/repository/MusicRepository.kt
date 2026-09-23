package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.audio.AudioSynthesizer
import com.example.data.local.AudioPresetEntity
import com.example.data.local.MidnightDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.TrackEntity
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MusicRepository(private val context: Context) {

  private val tag = "MusicRepository"
  private val database = MidnightDatabase.getDatabase(context)
  private val trackDao = database.trackDao()
  private val playlistDao = database.playlistDao()
  private val presetDao = database.presetDao()

  val allTracks: Flow<List<Track>> = trackDao.getAllTracks().map { entities ->
    entities.map { it.toModel() }
  }

  val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks().map { entities ->
    entities.map { it.toModel() }
  }

  val mostPlayedTracks: Flow<List<Track>> = trackDao.getMostPlayedTracks().map { entities ->
    entities.map { it.toModel() }
  }

  val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
  val allPresets: Flow<List<AudioPresetEntity>> = presetDao.getAllPresets()

  fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> =
    playlistDao.getTracksForPlaylist(playlistId).map { entities ->
      entities.map { it.toModel() }
    }

  suspend fun initializeDefaultsIfEmpty() = withContext(Dispatchers.IO) {
    // 1. Check presets
    val presetCount = presetDao.getPresetCount()
    if (presetCount == 0) {
      val defaultPresets = listOf(
        AudioPresetEntity(
          name = "Midnight Bass Signature",
          bassBoostStrength = 850,
          virtualizerStrength = 400,
          eqBandLevels = "1200,800,-200,300,600",
          isMidnightBassMode = true,
          isPresetReadOnly = true
        ),
        AudioPresetEntity(
          name = "Sub Overdrive",
          bassBoostStrength = 1000,
          virtualizerStrength = 200,
          eqBandLevels = "1500,1000,-300,0,400",
          isMidnightBassMode = true,
          isPresetReadOnly = true
        ),
        AudioPresetEntity(
          name = "Club & EDM",
          bassBoostStrength = 750,
          virtualizerStrength = 500,
          eqBandLevels = "900,500,-200,600,900",
          isMidnightBassMode = false,
          isPresetReadOnly = true
        ),
        AudioPresetEntity(
          name = "Rock Punch",
          bassBoostStrength = 600,
          virtualizerStrength = 300,
          eqBandLevels = "600,400,200,500,700",
          isMidnightBassMode = false,
          isPresetReadOnly = true
        ),
        AudioPresetEntity(
          name = "Hip-Hop 808",
          bassBoostStrength = 900,
          virtualizerStrength = 350,
          eqBandLevels = "1100,700,0,300,500",
          isMidnightBassMode = true,
          isPresetReadOnly = true
        ),
        AudioPresetEntity(
          name = "Flat / Bypass",
          bassBoostStrength = 0,
          virtualizerStrength = 0,
          eqBandLevels = "0,0,0,0,0",
          isMidnightBassMode = false,
          isPresetReadOnly = true
        )
      )
      presetDao.insertPresets(defaultPresets)
    }

    // 2. Check tracks
    val trackCount = trackDao.getTrackCount()
    if (trackCount == 0) {
      val sampleTracks = AudioSynthesizer.ensureSampleTracks(context)
      trackDao.insertTracks(sampleTracks)

      // Create default starter playlist
      val playlistId = playlistDao.insertPlaylist(
        PlaylistEntity(
          name = "Midnight Bass Essentials",
          description = "Heavy sub-bass, 808 grooves, and synthwave vibes."
        )
      )
      sampleTracks.forEachIndexed { index, trackEntity ->
        playlistDao.addTrackToPlaylist(
          PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackEntity.id,
            orderIndex = index
          )
        )
      }
    }
  }

  suspend fun scanDeviceAudio(): Int = withContext(Dispatchers.IO) {
    var scannedCount = 0
    val projection = arrayOf(
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.ALBUM,
      MediaStore.Audio.Media.DURATION,
      MediaStore.Audio.Media.YEAR
    )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"

    try {
      val cursor = context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        "${MediaStore.Audio.Media.DATE_ADDED} DESC"
      )

      cursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val yearCol = it.getColumnIndex(MediaStore.Audio.Media.YEAR)

        val newTracks = mutableListOf<TrackEntity>()
        while (it.moveToNext()) {
          val mediaId = it.getLong(idCol)
          val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            mediaId
          ).toString()

          val title = it.getString(titleCol) ?: "Unknown Track"
          val artist = it.getString(artistCol) ?: "Unknown Artist"
          val album = it.getString(albumCol) ?: "Unknown Album"
          val duration = it.getLong(durationCol)
          val year = if (yearCol != -1) it.getInt(yearCol) else null

          // Check if already in DB
          if (trackDao.getTrackByUri(contentUri) == null) {
            newTracks.add(
              TrackEntity(
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                uri = contentUri,
                year = year,
                isDemo = false
              )
            )
            scannedCount++
          }
        }
        if (newTracks.isNotEmpty()) {
          trackDao.insertTracks(newTracks)
        }
      }
    } catch (e: Exception) {
      Log.e(tag, "Device scan failed: ${e.message}", e)
    }

    scannedCount
  }

  suspend fun importAudioUri(uri: Uri): TrackEntity? = withContext(Dispatchers.IO) {
    try {
      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(context, uri)

      val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
        ?: "Imported Track"
      val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
      val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Imported Files"
      val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "Bass / Audio"
      val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val duration = durationStr?.toLongOrNull() ?: 0L

      // Copy file to app storage to ensure perpetual read access
      val importedDir = File(context.filesDir, "imported_music")
      if (!importedDir.exists()) importedDir.mkdirs()

      val cleanFileName = "import_${System.currentTimeMillis()}_${(0..999).random()}.mp3"
      val targetFile = File(importedDir, cleanFileName)

      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }

      val track = TrackEntity(
        title = title,
        artist = artist,
        album = album,
        genre = genre,
        durationMs = if (duration > 0) duration else 60_000L,
        uri = targetFile.absolutePath,
        isFavorite = false,
        isDemo = false
      )
      val id = trackDao.insertTrack(track)
      retriever.release()
      track.copy(id = id)
    } catch (e: Exception) {
      Log.e(tag, "Failed to import URI: ${e.message}", e)
      null
    }
  }

  suspend fun insertTrack(track: TrackEntity): Long = withContext(Dispatchers.IO) {
    trackDao.insertTrack(track)
  }

  suspend fun toggleFavorite(trackId: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
    trackDao.setFavorite(trackId, !currentIsFavorite)
  }

  suspend fun updateTrackMetadata(id: Long, title: String, artist: String, album: String, genre: String?, year: Int?) =
    withContext(Dispatchers.IO) {
      trackDao.updateMetadata(id, title, artist, album, genre, year)
    }

  suspend fun deleteTrack(trackId: Long) = withContext(Dispatchers.IO) {
    trackDao.deleteTrackById(trackId)
  }

  suspend fun incrementPlayCount(trackId: Long) = withContext(Dispatchers.IO) {
    trackDao.incrementPlayCount(trackId)
  }

  // Playlists
  suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
    playlistDao.insertPlaylist(
      PlaylistEntity(
        name = name,
        description = description
      )
    )
  }

  suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
    playlistDao.deletePlaylistById(playlistId)
  }

  suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
    playlistDao.addTrackToPlaylist(
      PlaylistTrackCrossRef(
        playlistId = playlistId,
        trackId = trackId
      )
    )
  }

  suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
    playlistDao.removeTrackFromPlaylist(playlistId, trackId)
  }

  private fun TrackEntity.toModel(): Track = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    uri = uri,
    albumArtUri = albumArtUri,
    genre = genre,
    year = year,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    playCount = playCount,
    isDemo = isDemo,
    customBpm = customBpm
  )

  fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    uri = uri,
    albumArtUri = albumArtUri,
    genre = genre,
    year = year,
    isFavorite = isFavorite,
    dateAdded = dateAdded,
    playCount = playCount,
    isDemo = isDemo,
    customBpm = customBpm
  )
}
