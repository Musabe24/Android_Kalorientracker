package com.example.kalorientracker.domain.calorie

import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieOverviewUseCaseTest {
    @Test
    fun `invoke returns entries with calculated summary`() {
        val repository = StaticCalorieEntryRepository(
            entries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 700,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 19810L
                ),
                CalorieEntry(
                    id = "entry-2",
                    amount = 250,
                    type = CalorieEntryType.BURNED,
                    source = CalorieEntrySource.WATCH,
                    recordedOnEpochDay = 19810L
                )
            )
        )
        val useCase = LoadCalorieOverviewUseCase(
            repository = repository,
            dailyCalorieCalculator = DailyCalorieCalculator()
        )

        val overview = useCase()

        assertEquals(2, overview.entries.size)
        assertEquals(700, overview.summary.totalIntake)
        assertEquals(250, overview.summary.totalBurned)
        assertEquals(450, overview.summary.netCalories)
    }
}

private class StaticCalorieEntryRepository(
    private val entries: List<CalorieEntry>
) : CalorieEntryRepository {
    override fun getEntries(): List<CalorieEntry> = entries

    override fun saveEntry(entry: CalorieEntry) {
        throw UnsupportedOperationException("Not needed for this test.")
    }

    override fun deleteEntry(entryId: String) {
        throw UnsupportedOperationException("Not needed for this test.")
    }
}
