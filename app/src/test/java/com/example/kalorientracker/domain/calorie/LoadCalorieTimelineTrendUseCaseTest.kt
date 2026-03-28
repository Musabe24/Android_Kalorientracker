package com.example.kalorientracker.domain.calorie

import com.example.kalorientracker.testutil.InMemoryCalorieEntryRepository
import com.example.kalorientracker.testutil.fixedTestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieTimelineTrendUseCaseTest {
    @Test
    fun `invoke returns empty list when no entries exist`() = runTest {
        val useCase = LoadCalorieTimelineTrendUseCase(
            repository = TimelineTrendRepository(entries = emptyList()),
            dailyCalorieCalculator = DailyCalorieCalculator(),
            clock = fixedTestClock()
        )

        val trend = useCase()

        assertEquals(emptyList<DailyCalorieTrendPoint>(), trend)
    }

    @Test
    fun `invoke returns all tracked days up to today including empty days`() = runTest {
        val repository = TimelineTrendRepository(
            entries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 700,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20536L
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
        val useCase = LoadCalorieTimelineTrendUseCase(
            repository = repository,
            dailyCalorieCalculator = DailyCalorieCalculator(),
            clock = fixedTestClock()
        )

        val trend = useCase()

        assertEquals(5, trend.size)
        assertEquals(20536L, trend.first().epochDay)
        assertEquals(20540L, trend.last().epochDay)
        assertEquals(700, trend.first().totalIntake)
        assertEquals(0, trend[1].netCalories)
        assertEquals(250, trend.last().totalBurned)
    }
}

private class TimelineTrendRepository(entries: List<CalorieEntry>) :
    InMemoryCalorieEntryRepository(entries)
