package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.GoalTargetRepository

class RoomGoalTargetRepository(
    private val goalSettingsDao: GoalSettingsDao
) : GoalTargetRepository {
    override suspend fun getTargetCalories(): Int {
        return goalSettingsDao.getById()?.targetCalories
            ?: CalculateGoalProgressUseCase.DEFAULT_TARGET_CALORIES
    }

    override suspend fun setTargetCalories(targetCalories: Int) {
        goalSettingsDao.upsert(
            GoalSettingsEntity(targetCalories = targetCalories)
        )
    }
}
