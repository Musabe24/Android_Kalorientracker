package com.example.kalorientracker.domain.calorie

/**
 * Parses and validates user calorie input.
 *
 * Contract:
 * - Blank values are invalid.
 * - Non-integer values are invalid.
 * - Negative values are invalid.
 * - Valid values are returned as [ValidationResult.Valid].
 */
class CalorieInputValidator {
    fun validate(rawValue: String): ValidationResult {
        if (rawValue.isBlank()) {
            return ValidationResult.Invalid("Input must not be blank.")
        }

        val parsedCalories = rawValue.toIntOrNull()
            ?: return ValidationResult.Invalid("Input must be a whole number.")

        if (parsedCalories < 0) {
            return ValidationResult.Invalid("Calories must be zero or positive.")
        }

        return ValidationResult.Valid(parsedCalories)
    }
}

sealed interface ValidationResult {
    data class Valid(val calories: Int) : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}
