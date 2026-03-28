package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerViewModelTest {
    @Test
    fun `view model starts with first day and no entries`() {
        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.dayNumber)
        assertTrue(uiState.entries.isEmpty())
    }

    @Test
    fun `save entry updates summary for intake`() {
        val viewModel = createViewModel()

        viewModel.onCalorieInputChanged("500")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        viewModel.saveEntry()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals(500, uiState.totalIntake)
        assertEquals(0, uiState.totalBurned)
        assertEquals(500, uiState.netCalories)
        assertEquals(20540L, uiState.entries.single().recordedOnEpochDay)
    }

    @Test
    fun `save entry updates summary for burned calories`() {
        val viewModel = createViewModel()

        viewModel.onCalorieInputChanged("600")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.saveEntry()

        viewModel.onCalorieInputChanged("250")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.onEntrySourceSelected(CalorieEntrySource.WATCH)
        viewModel.saveEntry()

        val uiState = viewModel.uiState.value
        assertEquals(600, uiState.totalIntake)
        assertEquals(250, uiState.totalBurned)
        assertEquals(350, uiState.netCalories)
    }

    @Test
    fun `save entry with invalid calories exposes input error`() {
        val viewModel = createViewModel()

        viewModel.onCalorieInputChanged("0")
        viewModel.saveEntry()

        assertNotNull(viewModel.uiState.value.inputError)
    }

    @Test
    fun `start editing populates form and save updates existing entry`() {
        val repository = InMemoryCalorieEntryRepository(
            entries = mutableListOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 300,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 19810L
                )
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.startEditing(repository.getEntries().single())
        viewModel.onCalorieInputChanged("450")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.saveEntry()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals(450, uiState.entries.single().amount)
        assertEquals(CalorieEntryType.BURNED, uiState.entries.single().type)
        assertNull(uiState.editingEntryId)
    }

    @Test
    fun `delete entry removes it from overview and clears active edit state`() {
        val repository = InMemoryCalorieEntryRepository(
            entries = mutableListOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 220,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MANUAL,
                    recordedOnEpochDay = 19810L
                )
            )
        )
        val viewModel = createViewModel(repository)

        viewModel.startEditing(repository.getEntries().single())
        viewModel.deleteEntry("entry-1")

        val uiState = viewModel.uiState.value
        assertTrue(uiState.entries.isEmpty())
        assertNull(uiState.editingEntryId)
        assertEquals("", uiState.calorieInput)
    }

    private fun createViewModel(
        repository: CalorieEntryRepository = InMemoryCalorieEntryRepository()
    ): TrackerViewModel {
        return TrackerViewModel(
            saveCalorieEntryUseCase = SaveCalorieEntryUseCase(
                repository = repository,
                inputValidator = CalorieInputValidator(),
                clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
            ),
            deleteCalorieEntryUseCase = DeleteCalorieEntryUseCase(
                repository = repository
            ),
            loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator()
            )
        )
    }
}

private class InMemoryCalorieEntryRepository(
    private val entries: MutableList<CalorieEntry> = mutableListOf()
) : CalorieEntryRepository {
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
