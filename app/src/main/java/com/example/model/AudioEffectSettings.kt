package com.example.model

data class EqBand(
  val index: Short,
  val centerFreqHz: Int,
  val levelMilliBels: Short,
  val minMilliBels: Short = -1500,
  val maxMilliBels: Short = 1500
) {
  val label: String
    get() = when {
      centerFreqHz >= 1000000 -> "${centerFreqHz / 1000000}M"
      centerFreqHz >= 1000 -> "${centerFreqHz / 1000}k"
      else -> "${centerFreqHz}Hz"
    }

  val dbGain: Float
    get() = levelMilliBels / 100f
}

data class AudioEffectSettings(
  val isEnabled: Boolean = true,
  val bassBoostStrength: Int = 650, // 0..1000 default to punchy bass!
  val isMidnightSubBass: Boolean = true,
  val virtualizerStrength: Int = 400, // 0..1000
  val eqEnabled: Boolean = true,
  val eqBands: List<EqBand> = listOf(
    EqBand(0, 60, 800),    // Sub-bass boost +8dB
    EqBand(1, 230, 500),   // Bass +5dB
    EqBand(2, 910, -200),  // Mid scoop -2dB
    EqBand(3, 3600, 300),  // Presence +3dB
    EqBand(4, 14000, 600)  // Air/Cymbal sparkle +6dB
  ),
  val currentPresetName: String = "Midnight Bass Signature",
  val playbackSpeed: Float = 1.0f,
  val playbackPitch: Float = 1.0f,
  val reverbPreset: Short = 0,
  val reverbPresetName: String = "None",
  val preAmpGainDb: Float = 2.0f
)
