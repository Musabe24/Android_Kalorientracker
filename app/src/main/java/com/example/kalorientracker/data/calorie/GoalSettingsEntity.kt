package com.example.kalorientracker.data.calorie

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_settings")
data class GoalSettingsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val targetCalories: Int
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
