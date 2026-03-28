package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.flow.Flow

class LoadGoalTargetUseCase(
    private val repository: GoalTargetRepository
) {
    fun observe(): Flow<Int> = repository.observeTargetCalories()

    suspend operator fun invoke(): Int = repository.getTargetCalories()
}
