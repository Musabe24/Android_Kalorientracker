package com.example.kalorientracker.domain.calorie

import com.example.kalorientracker.testutil.InMemoryCalorieEntryRepository
import com.example.kalorientracker.testutil.fixedTestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadWeeklyCalorieTrendUseCaseTest {
    @Test
    fun `invoke returns seven days with zeros for missing days`() = runTest {
        val repository = WeeklyTrendRepository(
            entries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 700,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20538L
                ),
                CalorieEntry(
                    id = "entry-2",
                    amount = 250,
                    type = CalorieEntryType.BURNED,
                    source = CalorieEntrySource.WATCH,
                    recordedOnEpochDay = 20540L
                )
            )
        )
        val useCase = LoadWeeklyCalorieTrendUseCase(
            repository = repository,
            dailyCalorieCalculator = DailyCalorieCalculator(),
            clock = fixedTestClock()
        )

        val trend = useCase()

        assertEquals(7, trend.size)
        assertEquals(20534L, trend.first().epochDay)
        assertEquals(20540L, trend.last().epochDay)
        assertEquals(700, trend[4].totalIntake)
        assertEquals(0, trend[5].totalIntake)
        assertEquals(250, trend[6].totalBurned)
    }
}

private class WeeklyTrendRepository(entries: List<CalorieEntry>) :
    InMemoryCalorieEntryRepository(entries)
