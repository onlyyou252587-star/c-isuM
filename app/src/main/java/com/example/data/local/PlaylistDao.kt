package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
  @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
  fun getAllPlaylists(): Flow<List<PlaylistEntity>>

  @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
  fun getPlaylistById(id: Long): Flow<PlaylistEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlaylist(playlist: PlaylistEntity): Long

  @Delete
  suspend fun deletePlaylist(playlist: PlaylistEntity)

  @Query("DELETE FROM playlists WHERE id = :id")
  suspend fun deletePlaylistById(id: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

  @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
  suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

  @Query("""
    SELECT t.* FROM tracks t
    INNER JOIN playlist_tracks pt ON t.id = pt.trackId
    WHERE pt.playlistId = :playlistId
    ORDER BY pt.orderIndex ASC
  """)
  fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

  @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
  fun getTrackCountForPlaylist(playlistId: Long): Flow<Int>
}
