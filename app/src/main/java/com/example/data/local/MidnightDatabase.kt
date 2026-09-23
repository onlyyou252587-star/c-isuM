package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    TrackEntity::class,
    PlaylistEntity::class,
    PlaylistTrackCrossRef::class,
    AudioPresetEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class MidnightDatabase : RoomDatabase() {
  abstract fun trackDao(): TrackDao
  abstract fun playlistDao(): PlaylistDao
  abstract fun presetDao(): PresetDao

  companion object {
    @Volatile
    private var INSTANCE: MidnightDatabase? = null

    fun getDatabase(context: Context): MidnightDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MidnightDatabase::class.java,
          "midnight_bass.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
