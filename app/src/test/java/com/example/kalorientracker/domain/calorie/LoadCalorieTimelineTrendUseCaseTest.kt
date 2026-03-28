package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCalorieTimelineTrendUseCaseTest {
    @Test
    fun `invoke returns empty list when no entries exist`() = runTest {
        val useCase = LoadCalorieTimelineTrendUseCase(
            repository = TimelineTrendRepository(entries = emptyList()),
            dailyCalorieCalculator = DailyCalorieCalculator(),
            clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
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
            clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
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

private class TimelineTrendRepository(
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
