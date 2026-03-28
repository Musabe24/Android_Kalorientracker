package com.example.kalorientracker.domain.calorie

class UpdateGoalTargetUseCase(
    private val repository: GoalTargetRepository
) {
    suspend operator fun invoke(rawTargetCalories: String): UpdateGoalTargetResult {
        if (rawTargetCalories.isBlank()) {
            return UpdateGoalTargetResult.ValidationError("Target must not be blank.")
        }

        val targetCalories = rawTargetCalories.toIntOrNull()
            ?: return UpdateGoalTargetResult.ValidationError("Target must be a whole number.")

        if (targetCalories <= 0) {
            return UpdateGoalTargetResult.ValidationError("Target must be greater than zero.")
        }

        repository.setTargetCalories(targetCalories)
        return UpdateGoalTargetResult.Success(targetCalories)
    }
}

sealed interface UpdateGoalTargetResult {
    data class Success(val targetCalories: Int) : UpdateGoalTargetResult
    data class ValidationError(val message: String) : UpdateGoalTargetResult
}
