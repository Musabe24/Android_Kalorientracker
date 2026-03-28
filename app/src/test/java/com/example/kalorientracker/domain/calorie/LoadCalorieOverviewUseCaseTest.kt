package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieOverviewUseCaseTest {
    @Test
    fun `invoke returns entries with calculated summary`() = runTest {
        val repository = StaticCalorieEntryRepository(
            entries = listOf(
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
            clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
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
    override fun observeEntries(): Flow<List<CalorieEntry>> = flowOf(entries)

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
