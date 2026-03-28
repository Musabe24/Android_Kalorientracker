package com.example.kalorientracker.domain.calorie

class LoadGoalTargetUseCase(
    private val repository: GoalTargetRepository
) {
    suspend operator fun invoke(): Int = repository.getTargetCalories()
}
