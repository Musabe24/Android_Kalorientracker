package com.example.kalorientracker.data.calorie

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CalorieEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CalorieTrackerDatabase : RoomDatabase() {
    abstract fun calorieEntryDao(): CalorieEntryDao
}
