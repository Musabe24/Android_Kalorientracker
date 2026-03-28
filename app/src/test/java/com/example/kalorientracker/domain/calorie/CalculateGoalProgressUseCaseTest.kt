package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateGoalProgressUseCaseTest {
    private val useCase = CalculateGoalProgressUseCase()

    @Test
    fun `invoke calculates remaining average hits and streak`() {
        val result = useCase(
            netCalories = 1850,
            weeklyTrend = listOf(
                DailyCalorieTrendPoint(20534L, 1800, 200, 1600),
                DailyCalorieTrendPoint(20535L, 2100, 100, 2000),
                DailyCalorieTrendPoint(20536L, 1900, 200, 1700),
                DailyCalorieTrendPoint(20537L, 0, 0, 0),
                DailyCalorieTrendPoint(20538L, 2200, 100, 2100),
                DailyCalorieTrendPoint(20539L, 2000, 100, 1900),
                DailyCalorieTrendPoint(20540L, 1950, 100, 1850)
            ),
            targetCalories = 2200
        )

        assertEquals(350, result.remainingCalories)
        assertEquals(1858, result.averageNetCalories)
        assertEquals(6, result.targetHitDays)
        assertEquals(3, result.consistencyStreak)
    }
}
