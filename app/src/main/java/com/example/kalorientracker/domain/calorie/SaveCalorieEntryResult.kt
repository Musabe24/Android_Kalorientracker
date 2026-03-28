package com.example.kalorientracker.domain.calorie

sealed interface SaveCalorieEntryResult {
    data object Success : SaveCalorieEntryResult

    data class ValidationError(val reason: CalorieInputValidationError) : SaveCalorieEntryResult
}
