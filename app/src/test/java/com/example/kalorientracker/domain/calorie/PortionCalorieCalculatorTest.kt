package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortionCalorieCalculatorTest {
    private val calculator = PortionCalorieCalculator(
        inputValidator = CalorieInputValidator()
    )

    @Test
    fun `calculate returns rounded calories for a valid portion`() {
        val result = calculator.calculate(
            rawConsumedAmount = "140",
            rawCaloriesPer100Units = "64"
        )

        assertEquals(PortionCalorieCalculationResult.Valid(90), result)
    }

    @Test
    fun `calculate returns input specific validation errors`() {
        val result = calculator.calculate(
            rawConsumedAmount = "",
            rawCaloriesPer100Units = "0"
        )

        assertTrue(result is PortionCalorieCalculationResult.Invalid)
        result as PortionCalorieCalculationResult.Invalid
        assertEquals(CalorieInputValidationError.Blank, result.errors.consumedAmountError)
        assertEquals(CalorieInputValidationError.NonPositive, result.errors.caloriesPer100UnitsError)
    }
}
