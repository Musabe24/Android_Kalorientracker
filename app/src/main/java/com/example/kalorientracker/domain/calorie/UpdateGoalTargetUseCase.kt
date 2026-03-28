package com.example.kalorientracker.domain.calorie

class UpdateGoalTargetUseCase(
    private val repository: GoalTargetRepository
) {
    suspend operator fun invoke(rawTargetCalories: String): UpdateGoalTargetResult {
        if (rawTargetCalories.isBlank()) {
            return UpdateGoalTargetResult.ValidationError(GoalTargetValidationError.Blank)
        }

        val targetCalories = rawTargetCalories.toIntOrNull()
            ?: return UpdateGoalTargetResult.ValidationError(GoalTargetValidationError.NotWholeNumber)

        if (targetCalories <= 0) {
            return UpdateGoalTargetResult.ValidationError(GoalTargetValidationError.NonPositive)
        }

        repository.setTargetCalories(targetCalories)
        return UpdateGoalTargetResult.Success(targetCalories)
    }
}

enum class GoalTargetValidationError {
    Blank,
    NotWholeNumber,
    NonPositive
}

sealed interface UpdateGoalTargetResult {
    data class Success(val targetCalories: Int) : UpdateGoalTargetResult
    data class ValidationError(val reason: GoalTargetValidationError) : UpdateGoalTargetResult
}
