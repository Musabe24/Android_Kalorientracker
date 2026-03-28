package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyCalorieCalculatorTest {
    private val calculator = DailyCalorieCalculator()

    @Test
    fun `calculate summary returns intake burned and net totals`() {
        val entries = listOf(
            CalorieEntry(amount = 400, type = CalorieEntryType.INTAKE),
            CalorieEntry(amount = 550, type = CalorieEntryType.INTAKE),
            CalorieEntry(amount = 300, type = CalorieEntryType.BURNED)
        )

        val summary = calculator.calculateSummary(entries)

        assertEquals(950, summary.totalIntake)
        assertEquals(300, summary.totalBurned)
        assertEquals(650, summary.netCalories)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculate summary throws when entry has zero calories`() {
        val entries = listOf(CalorieEntry(amount = 0, type = CalorieEntryType.INTAKE))

        calculator.calculateSummary(entries)
    }
}
