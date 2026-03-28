package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieInputValidatorTest {
    private val validator = CalorieInputValidator()

    @Test
    fun `validate returns valid result for positive number`() {
        val result = validator.validate("345")

        assertEquals(ValidationResult.Valid(345), result)
    }

    @Test
    fun `validate returns invalid result for blank input`() {
        val result = validator.validate("   ")

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validate returns invalid result for non numeric input`() {
        val result = validator.validate("abc")

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validate returns invalid result for zero`() {
        val result = validator.validate("0")

        assertTrue(result is ValidationResult.Invalid)
    }
}
