package com.example.kalorientracker.domain.calorie

enum class GoalTargetValidationError {
    Blank,
    NotWholeNumber,
    NonPositive
}

sealed interface UpdateGoalTargetResult {
    data class Success(val targetCalories: Int) : UpdateGoalTargetResult
    data class ValidationError(val reason: GoalTargetValidationError) : UpdateGoalTargetResult
}
