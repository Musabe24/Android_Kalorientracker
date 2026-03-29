package com.example.kalorientracker.domain.calorie

import kotlin.math.roundToInt

/**
 * Calculates total calories from a consumed portion and a package reference value per 100 units.
 *
 * Contract:
 * - Both raw inputs must be valid positive whole numbers.
 * - Returned calories are rounded to the nearest whole calorie.
 * - Validation errors stay attached to the field that caused them.
 */
class PortionCalorieCalculator(
    private val inputValidator: CalorieInputValidator
) {
    fun calculate(
        rawConsumedAmount: String,
        rawCaloriesPer100Units: String
    ): PortionCalorieCalculationResult {
        val consumedAmount = inputValidator.validate(rawConsumedAmount)
        val caloriesPer100Units = inputValidator.validate(rawCaloriesPer100Units)

        if (consumedAmount is ValidationResult.Invalid || caloriesPer100Units is ValidationResult.Invalid) {
            return PortionCalorieCalculationResult.Invalid(
                PortionCalorieInputErrors(
                    consumedAmountError = (consumedAmount as? ValidationResult.Invalid)?.reason,
                    caloriesPer100UnitsError = (caloriesPer100Units as? ValidationResult.Invalid)?.reason
                )
            )
        }

        consumedAmount as ValidationResult.Valid
        caloriesPer100Units as ValidationResult.Valid

        val calculatedCalories = (consumedAmount.calories * caloriesPer100Units.calories / 100.0)
            .roundToInt()

        return PortionCalorieCalculationResult.Valid(calculatedCalories)
    }
}

data class PortionCalorieInputErrors(
    val consumedAmountError: CalorieInputValidationError? = null,
    val caloriesPer100UnitsError: CalorieInputValidationError? = null
)

sealed interface PortionCalorieCalculationResult {
    data class Valid(val calories: Int) : PortionCalorieCalculationResult
    data class Invalid(val errors: PortionCalorieInputErrors) : PortionCalorieCalculationResult
}
