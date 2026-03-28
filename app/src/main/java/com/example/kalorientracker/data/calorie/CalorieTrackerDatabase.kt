package com.example.kalorientracker.data.calorie

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CalorieEntryEntity::class, GoalSettingsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class CalorieTrackerDatabase : RoomDatabase() {
    abstract fun calorieEntryDao(): CalorieEntryDao
    abstract fun goalSettingsDao(): GoalSettingsDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS goal_settings (
                        id INTEGER NOT NULL,
                        targetCalories INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "INSERT OR IGNORE INTO goal_settings (id, targetCalories) VALUES (1, 2200)"
                )
            }
        }

        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE calorie_entries
                    ADD COLUMN name TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }
    }
}
