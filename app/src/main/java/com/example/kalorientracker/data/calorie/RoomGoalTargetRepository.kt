package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.GoalTargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomGoalTargetRepository(
    private val goalSettingsDao: GoalSettingsDao
) : GoalTargetRepository {
    override fun observeTargetCalories(): Flow<Int> {
        return goalSettingsDao.observeById().map { settings ->
            settings?.targetCalories ?: CalculateGoalProgressUseCase.DEFAULT_TARGET_CALORIES
        }
    }

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
