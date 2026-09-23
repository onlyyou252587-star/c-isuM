package com.example.model

enum class RepeatMode {
  OFF, ALL, ONE
}

data class PlaybackState(
  val currentTrack: Track? = null,
  val isPlaying: Boolean = false,
  val currentPositionMs: Long = 0,
  val durationMs: Long = 0,
  val isShuffleEnabled: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.ALL,
  val sleepTimerMinutesLeft: Int? = null,
  val visualizerAmplitudes: List<Float> = List(20) { 0.05f }
) {
  val progress: Float
    get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

  val formattedPosition: String
    get() {
      val totalSeconds = (currentPositionMs / 1000).coerceAtLeast(0)
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return "%02d:%02d".format(minutes, seconds)
    }

  val formattedRemaining: String
    get() {
      val remainingSeconds = ((durationMs - currentPositionMs) / 1000).coerceAtLeast(0)
      val minutes = remainingSeconds / 60
      val seconds = remainingSeconds % 60
      return "-%02d:%02d".format(minutes, seconds)
    }
}
