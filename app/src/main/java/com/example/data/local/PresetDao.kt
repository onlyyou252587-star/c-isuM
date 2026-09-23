package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
  @Query("SELECT * FROM audio_presets ORDER BY id ASC")
  fun getAllPresets(): Flow<List<AudioPresetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPreset(preset: AudioPresetEntity): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPresets(presets: List<AudioPresetEntity>)

  @Delete
  suspend fun deletePreset(preset: AudioPresetEntity)

  @Query("SELECT COUNT(*) FROM audio_presets")
  suspend fun getPresetCount(): Int
}
