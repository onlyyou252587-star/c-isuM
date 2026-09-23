package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val description: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val coverColorHex: Long = 0xFF00E5FF
)

@Entity(
  tableName = "playlist_tracks",
  primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
  val playlistId: Long,
  val trackId: Long,
  val orderIndex: Int = 0
)
