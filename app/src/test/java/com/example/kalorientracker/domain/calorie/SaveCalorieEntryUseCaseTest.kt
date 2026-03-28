package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCalorieEntryUseCaseTest {
    private val repository = FakeCalorieEntryRepository()
    private val useCase = SaveCalorieEntryUseCase(
        repository = repository,
        inputValidator = CalorieInputValidator(),
        clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
    )

    @Test
    fun `invoke persists valid intake entry with tracked day`() = runTest {
        val result = useCase(
            rawName = "Overnight oats",
            rawCalories = "420",
            entryType = CalorieEntryType.INTAKE,
            entrySource = CalorieEntrySource.MEAL,
            editingEntryId = null,
            editingEntryRecordedOnEpochDay = null
        )

        assertEquals(SaveCalorieEntryResult.Success, result)
        assertEquals(1, repository.getEntries().size)
        assertEquals("Overnight oats", repository.getEntries().single().name)
        assertEquals(420, repository.getEntries().single().amount)
        assertEquals(20540L, repository.getEntries().single().recordedOnEpochDay)
    }

    @Test
    fun `invoke updates an existing entry instead of appending a second one`() = runTest {
        repository.saveEntry(
            CalorieEntry(
                id = "entry-1",
                name = "Lunch",
                amount = 300,
                type = CalorieEntryType.INTAKE,
                source = CalorieEntrySource.MANUAL,
                recordedOnEpochDay = 19810L
            )
        )

        val result = useCase(
            rawName = "Run session",
            rawCalories = "510",
            entryType = CalorieEntryType.BURNED,
            entrySource = CalorieEntrySource.WATCH,
            editingEntryId = "entry-1",
            editingEntryRecordedOnEpochDay = 19810L
        )

        assertEquals(SaveCalorieEntryResult.Success, result)
        assertEquals(
            listOf(
                CalorieEntry(
                    id = "entry-1",
                    name = "Run session",
                    amount = 510,
                    type = CalorieEntryType.BURNED,
                    source = CalorieEntrySource.WATCH,
                    recordedOnEpochDay = 19810L
                )
            ),
            repository.getEntries()
        )
    }

    @Test
    fun `invoke returns validation error for invalid input`() = runTest {
        val result = useCase(
            rawName = "",
            rawCalories = "",
            entryType = CalorieEntryType.BURNED,
            entrySource = CalorieEntrySource.WATCH,
            editingEntryId = null,
            editingEntryRecordedOnEpochDay = null
        )

        assertTrue(result is SaveCalorieEntryResult.ValidationError)
        assertTrue(repository.getEntries().isEmpty())
    }
}

private class FakeCalorieEntryRepository : CalorieEntryRepository {
    private val entries = mutableListOf<CalorieEntry>()

    override suspend fun getEntries(): List<CalorieEntry> = entries.toList()

    override suspend fun getEntriesBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntry> {
        return entries.filter {
            it.recordedOnEpochDay in startEpochDayInclusive..endEpochDayInclusive
        }
    }

    override suspend fun saveEntry(entry: CalorieEntry) {
        val existingIndex = entries.indexOfFirst { it.id == entry.id }
        if (existingIndex >= 0) {
            entries[existingIndex] = entry
        } else {
            entries += entry
        }
    }

    override suspend fun deleteEntry(entryId: String) {
        entries.removeAll { it.id == entryId }
    }
}
