package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieHistoryUseCaseTest {
    @Test
    fun `invoke groups entries by day in descending order`() = runTest {
        val repository = HistoryRepository(
            entries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 300,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20539L
                ),
                CalorieEntry(
                    id = "entry-2",
                    amount = 200,
                    type = CalorieEntryType.BURNED,
                    source = CalorieEntrySource.WATCH,
                    recordedOnEpochDay = 20540L
                ),
                CalorieEntry(
                    id = "entry-3",
                    amount = 500,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MANUAL,
                    recordedOnEpochDay = 20540L
                )
            )
        )
        val useCase = LoadCalorieHistoryUseCase(
            repository = repository,
            dailyCalorieCalculator = DailyCalorieCalculator()
        )

        val history = useCase()

        assertEquals(2, history.size)
        assertEquals(20540L, history.first().epochDay)
        assertEquals(2, history.first().entries.size)
        assertEquals(500, history.first().totalIntake)
        assertEquals(200, history.first().totalBurned)
        assertEquals(300, history.first().netCalories)
    }
}

private class HistoryRepository(
    private val entries: List<CalorieEntry>
) : CalorieEntryRepository {
    override suspend fun getEntries(): List<CalorieEntry> = entries

    override suspend fun getEntriesBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntry> {
        return entries.filter {
            it.recordedOnEpochDay in startEpochDayInclusive..endEpochDayInclusive
        }
    }

    override suspend fun saveEntry(entry: CalorieEntry) {
        throw UnsupportedOperationException("Not needed for this test.")
    }

    override suspend fun deleteEntry(entryId: String) {
        throw UnsupportedOperationException("Not needed for this test.")
    }
}
