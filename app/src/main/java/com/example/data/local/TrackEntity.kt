package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val artist: String = "Unknown Artist",
  val album: String = "Midnight Collection",
  val durationMs: Long = 0,
  val uri: String, // file path, content URI, or demo ID
  val albumArtUri: String? = null,
  val genre: String? = "Bass / Electronic",
  val year: Int? = null,
  val isFavorite: Boolean = false,
  val dateAdded: Long = System.currentTimeMillis(),
  val playCount: Int = 0,
  val isDemo: Boolean = false,
  val customBpm: Int = 128
)
