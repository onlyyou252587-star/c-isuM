package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_presets")
data class AudioPresetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val bassBoostStrength: Int = 0, // 0..1000
  val virtualizerStrength: Int = 0, // 0..1000
  val eqBandLevels: String = "0,0,0,0,0", // comma separated millibels or relative values
  val speed: Float = 1.0f,
  val pitch: Float = 1.0f,
  val reverbPreset: Short = 0, // PresetReverb constants
  val isMidnightBassMode: Boolean = false,
  val isPresetReadOnly: Boolean = false
)
