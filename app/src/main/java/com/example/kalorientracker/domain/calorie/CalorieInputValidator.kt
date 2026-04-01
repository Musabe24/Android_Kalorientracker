package com.example.kalorientracker.domain.calorie

/**
 * Parses and validates user calorie input.
 *
 * Contract:
 * - Blank values are invalid.
 * - Non-integer values are invalid.
 * - Values smaller than 1 are invalid.
 * - Valid values are returned as [ValidationResult.Valid].
 */
class CalorieInputValidator {
    fun validate(rawValue: String): ValidationResult {
        if (rawValue.isBlank()) {
            return ValidationResult.Invalid(CalorieInputValidationError.Blank)
        }

        // Try to parse as integer first, then as double and round if needed
        val parsedCalories = rawValue.toIntOrNull()
            ?: rawValue.toDoubleOrNull()?.let { kotlin.math.round(it).toInt() }
            ?: return ValidationResult.Invalid(CalorieInputValidationError.NotWholeNumber)

        if (parsedCalories <= 0) {
            return ValidationResult.Invalid(CalorieInputValidationError.NonPositive)
        }

        return ValidationResult.Valid(parsedCalories)
    }
}
