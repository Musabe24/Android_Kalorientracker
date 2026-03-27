package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyCalorieCalculatorTest {
    private val calculator = DailyCalorieCalculator()

    @Test
    fun `total calories sums up all entries`() {
        val entries = listOf(MealEntry(400), MealEntry(550), MealEntry(150))

        val totalCalories = calculator.totalCalories(entries)

        assertEquals(1100, totalCalories)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `total calories throws when entry has negative calories`() {
        val entries = listOf(MealEntry(300), MealEntry(-20))

        calculator.totalCalories(entries)
    }
}
