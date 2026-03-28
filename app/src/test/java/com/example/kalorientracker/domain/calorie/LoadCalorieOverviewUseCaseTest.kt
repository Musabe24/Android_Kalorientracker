package com.example.kalorientracker.domain.calorie

import com.example.kalorientracker.testutil.InMemoryCalorieEntryRepository
import com.example.kalorientracker.testutil.fixedTestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieOverviewUseCaseTest {
    @Test
    fun `invoke returns entries with calculated summary`() = runTest {
        val repository = StaticCalorieEntryRepository(
            initialEntries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 700,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20540L
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
        val useCase = LoadCalorieOverviewUseCase(
            repository = repository,
            dailyCalorieCalculator = DailyCalorieCalculator(),
            clock = fixedTestClock()
        )

        val overview = useCase()

        assertEquals(2, overview.entries.size)
        assertEquals(700, overview.summary.totalIntake)
        assertEquals(250, overview.summary.totalBurned)
        assertEquals(450, overview.summary.netCalories)
    }
}

private class StaticCalorieEntryRepository(initialEntries: List<CalorieEntry>) :
    InMemoryCalorieEntryRepository(initialEntries)
