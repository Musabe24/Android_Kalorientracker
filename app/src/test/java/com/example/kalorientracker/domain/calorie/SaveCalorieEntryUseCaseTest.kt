package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
    fun `invoke persists valid intake entry with tracked day`() {
        val result = useCase(
            rawCalories = "420",
            entryType = CalorieEntryType.INTAKE,
            entrySource = CalorieEntrySource.MEAL,
            editingEntryId = null,
            editingEntryRecordedOnEpochDay = null
        )

        assertEquals(SaveCalorieEntryResult.Success, result)
        assertEquals(1, repository.getEntries().size)
        assertEquals(420, repository.getEntries().single().amount)
        assertEquals(20540L, repository.getEntries().single().recordedOnEpochDay)
    }

    @Test
    fun `invoke updates an existing entry instead of appending a second one`() {
        repository.saveEntry(
            CalorieEntry(
                id = "entry-1",
                amount = 300,
                type = CalorieEntryType.INTAKE,
                source = CalorieEntrySource.MANUAL,
                recordedOnEpochDay = 19810L
            )
        )

        val result = useCase(
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
    fun `invoke returns validation error for invalid input`() {
        val result = useCase(
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

    override fun getEntries(): List<CalorieEntry> = entries.toList()

    override fun saveEntry(entry: CalorieEntry) {
        val existingIndex = entries.indexOfFirst { it.id == entry.id }
        if (existingIndex >= 0) {
            entries[existingIndex] = entry
        } else {
            entries += entry
        }
    }

    override fun deleteEntry(entryId: String) {
        entries.removeAll { it.id == entryId }
    }
}
