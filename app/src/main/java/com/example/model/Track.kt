package com.example.model

data class Track(
  val id: Long = 0,
  val title: String,
  val artist: String = "Unknown Artist",
  val album: String = "Midnight Collection",
  val durationMs: Long = 0,
  val uri: String,
  val albumArtUri: String? = null,
  val genre: String? = "Bass / Electronic",
  val year: Int? = null,
  val isFavorite: Boolean = false,
  val dateAdded: Long = System.currentTimeMillis(),
  val playCount: Int = 0,
  val isDemo: Boolean = false,
  val customBpm: Int = 128
) {
  val formattedDuration: String
    get() {
      val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return "%02d:%02d".format(minutes, seconds)
    }
}
