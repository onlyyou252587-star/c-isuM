package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
  @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
  fun getAllTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
  fun getFavoriteTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT 20")
  fun getMostPlayedTracks(): Flow<List<TrackEntity>>

  @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
  fun getTrackById(id: Long): Flow<TrackEntity?>

  @Query("SELECT * FROM tracks WHERE uri = :uri LIMIT 1")
  suspend fun getTrackByUri(uri: String): TrackEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrack(track: TrackEntity): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertTracks(tracks: List<TrackEntity>)

  @Update
  suspend fun updateTrack(track: TrackEntity)

  @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: Long, isFavorite: Boolean)

  @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :id")
  suspend fun incrementPlayCount(id: Long)

  @Query("UPDATE tracks SET title = :title, artist = :artist, album = :album, genre = :genre, year = :year WHERE id = :id")
  suspend fun updateMetadata(id: Long, title: String, artist: String, album: String, genre: String?, year: Int?)

  @Delete
  suspend fun deleteTrack(track: TrackEntity)

  @Query("DELETE FROM tracks WHERE id = :id")
  suspend fun deleteTrackById(id: Long)

  @Query("SELECT COUNT(*) FROM tracks")
  suspend fun getTrackCount(): Int
}
