package com.example.kalorientracker.domain.calorie

enum class CalorieInputValidationError {
    Blank,
    NotWholeNumber,
    NonPositive
}

sealed interface ValidationResult {
    data class Valid(val calories: Int) : ValidationResult
    data class Invalid(val reason: CalorieInputValidationError) : ValidationResult
}
