package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import com.example.kalorientracker.testutil.MainDispatcherRule
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `view model starts with first day and no entries`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.dayNumber)
        assertTrue(uiState.entries.isEmpty())
        assertTrue(uiState.historyDays.isEmpty())
        assertEquals(7, uiState.weeklyTrend.size)
    }

    @Test
    fun `save entry updates summary for intake`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onCalorieInputChanged("500")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        viewModel.saveEntry()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals(500, uiState.totalIntake)
        assertEquals(0, uiState.totalBurned)
        assertEquals(500, uiState.netCalories)
        assertEquals(20540L, uiState.entries.single().recordedOnEpochDay)
    }

    @Test
    fun `save entry updates summary for burned calories`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onCalorieInputChanged("600")
        viewModel.onEntryTypeSelected(CalorieEntryType.INTAKE)
        viewModel.saveEntry()
        advanceUntilIdle()

        viewModel.onCalorieInputChanged("250")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.onEntrySourceSelected(CalorieEntrySource.WATCH)
        viewModel.saveEntry()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(600, uiState.totalIntake)
        assertEquals(250, uiState.totalBurned)
        assertEquals(350, uiState.netCalories)
    }

    @Test
    fun `save entry with invalid calories exposes input error`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onCalorieInputChanged("0")
        viewModel.saveEntry()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.inputError)
    }

    @Test
    fun `start editing populates form and save updates existing entry`() = runTest {
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

        advanceUntilIdle()
        viewModel.startEditing(repository.getEntries().single())
        viewModel.onCalorieInputChanged("450")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.saveEntry()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals(450, uiState.entries.single().amount)
        assertEquals(CalorieEntryType.BURNED, uiState.entries.single().type)
        assertNull(uiState.editingEntryId)
    }

    @Test
    fun `delete entry removes it from overview and clears active edit state`() = runTest {
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

        advanceUntilIdle()
        viewModel.startEditing(repository.getEntries().single())
        viewModel.deleteEntry("entry-1")
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.entries.isEmpty())
        assertNull(uiState.editingEntryId)
        assertEquals("", uiState.calorieInput)
    }

    @Test
    fun `request delete exposes pending dialog entry until dismissed`() = runTest {
        val entry = CalorieEntry(
            id = "entry-1",
            amount = 220,
            type = CalorieEntryType.INTAKE,
            source = CalorieEntrySource.MANUAL,
            recordedOnEpochDay = 20540L
        )
        val repository = InMemoryCalorieEntryRepository(entries = mutableListOf(entry))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.requestDeleteEntry(entry)
        assertEquals(entry, viewModel.uiState.value.pendingDeleteEntry)

        viewModel.dismissDeleteEntry()
        assertNull(viewModel.uiState.value.pendingDeleteEntry)
    }

    @Test
    fun `confirm delete removes pending entry`() = runTest {
        val entry = CalorieEntry(
            id = "entry-1",
            amount = 220,
            type = CalorieEntryType.INTAKE,
            source = CalorieEntrySource.MANUAL,
            recordedOnEpochDay = 20540L
        )
        val repository = InMemoryCalorieEntryRepository(entries = mutableListOf(entry))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        viewModel.requestDeleteEntry(entry)
        viewModel.confirmDeleteEntry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertNull(viewModel.uiState.value.pendingDeleteEntry)
    }

    @Test
    fun `select history filter narrows visible history days`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            entries = mutableListOf(
                CalorieEntry(
                    id = "today-entry",
                    amount = 300,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20540L
                ),
                CalorieEntry(
                    id = "older-entry",
                    amount = 200,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20533L
                )
            )
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredHistoryDays.size)

        viewModel.selectHistoryFilter(HistoryFilter.AllTime)
        assertEquals(2, viewModel.uiState.value.filteredHistoryDays.size)

        viewModel.selectHistoryFilter(HistoryFilter.Today)
        assertEquals(1, viewModel.uiState.value.filteredHistoryDays.size)
        assertEquals(20540L, viewModel.uiState.value.filteredHistoryDays.single().epochDay)
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
            loadCalorieHistoryUseCase = LoadCalorieHistoryUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator()
            ),
            loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator()
            ),
            loadWeeklyCalorieTrendUseCase = LoadWeeklyCalorieTrendUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator(),
                clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
            ),
            clock = Clock.fixed(Instant.parse("2026-03-28T10:15:30Z"), ZoneOffset.UTC)
        )
    }
}

private class InMemoryCalorieEntryRepository(
    private val entries: MutableList<CalorieEntry> = mutableListOf()
) : CalorieEntryRepository {
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
