package com.example.kalorientracker.data.calorie

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GoalSettingsDao {
    @Query("SELECT * FROM goal_settings WHERE id = :id")
    suspend fun getById(id: Int = GoalSettingsEntity.SINGLE_ROW_ID): GoalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: GoalSettingsEntity)
}
