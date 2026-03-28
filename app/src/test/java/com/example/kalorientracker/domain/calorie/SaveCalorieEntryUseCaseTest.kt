package com.example.kalorientracker.domain.calorie

import com.example.kalorientracker.testutil.InMemoryCalorieEntryRepository
import com.example.kalorientracker.testutil.fixedTestClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCalorieEntryUseCaseTest {
    private val repository = InMemoryCalorieEntryRepository()
    private val useCase = SaveCalorieEntryUseCase(
        repository = repository,
        inputValidator = CalorieInputValidator(),
        clock = fixedTestClock()
    )

    @Test
    fun `invoke persists valid intake entry with tracked day`() = runTest {
        val result = useCase(
            rawName = "Overnight oats",
            rawCalories = "420",
            entryType = CalorieEntryType.INTAKE,
            entrySource = CalorieEntrySource.MEAL,
            editingEntryId = null,
            recordedOnEpochDay = 20540L
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
            recordedOnEpochDay = 19810L
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
            recordedOnEpochDay = 20540L
        )

        assertTrue(result is SaveCalorieEntryResult.ValidationError)
        assertTrue(repository.getEntries().isEmpty())
    }
}
