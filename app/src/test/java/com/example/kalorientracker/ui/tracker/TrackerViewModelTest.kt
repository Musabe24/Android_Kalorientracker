package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidationError
import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.GoalTargetRepository
import com.example.kalorientracker.domain.calorie.GoalTargetValidationError
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieTimelineTrendUseCase
import com.example.kalorientracker.domain.calorie.LoadGoalTargetUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.PortionCalorieCalculator
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetUseCase
import com.example.kalorientracker.testutil.InMemoryCalorieEntryRepository
import com.example.kalorientracker.testutil.InMemoryGoalTargetRepository
import com.example.kalorientracker.testutil.MainDispatcherRule
import com.example.kalorientracker.testutil.fixedTestClock
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(28, uiState.dayNumber)
        assertTrue(uiState.entries.isEmpty())
        assertTrue(uiState.historyDays.isEmpty())
        assertTrue(uiState.timelineTrend.isEmpty())
        assertEquals(7, uiState.weeklyTrend.size)
        assertNotNull(uiState.goalProgressInsights)
    }

    @Test
    fun `save entry updates summary for intake`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onEntryNameChanged("Greek yogurt")
        viewModel.onCalorieInputChanged("500")
        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        viewModel.saveEntry()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals("Greek yogurt", uiState.entries.single().name)
        assertEquals(500, uiState.totalIntake)
        assertEquals(0, uiState.totalBurned)
        assertEquals(500, uiState.netCalories)
        assertEquals(20540L, uiState.entries.single().recordedOnEpochDay)
    }

    @Test
    fun `save entry updates summary for burned calories`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onCalorieInputChanged("600")
        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        viewModel.saveEntry()
        runCurrent()

        viewModel.onCalorieInputChanged("250")
        viewModel.onEntrySourceSelected(CalorieEntrySource.WATCH)
        viewModel.saveEntry()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(600, uiState.totalIntake)
        assertEquals(250, uiState.totalBurned)
        assertEquals(350, uiState.netCalories)
    }

    @Test
    fun `save entry with invalid calories exposes input error`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onCalorieInputChanged("0")
        viewModel.saveEntry()
        runCurrent()

        assertEquals(CalorieInputValidationError.NonPositive, viewModel.uiState.value.inputError)
    }

    @Test
    fun `portion calculator mode saves calculated calories`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onEntryInputModeSelected(EntryInputMode.PortionCalculator)
        viewModel.onEntryNameChanged("Milk")
        viewModel.onConsumedAmountInputChanged("140")
        viewModel.onCaloriesPer100InputChanged("64")
        viewModel.saveEntry()
        runCurrent()

        val entry = viewModel.uiState.value.entries.single()
        assertEquals("Milk", entry.name)
        assertEquals(90, entry.amount)
    }

    @Test
    fun `portion calculator mode exposes field specific errors`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onEntryInputModeSelected(EntryInputMode.PortionCalculator)
        viewModel.onConsumedAmountInputChanged("")
        viewModel.onCaloriesPer100InputChanged("0")
        viewModel.saveEntry()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(CalorieInputValidationError.Blank, uiState.consumedAmountInputError)
        assertEquals(CalorieInputValidationError.NonPositive, uiState.caloriesPer100InputError)
        assertTrue(uiState.entries.isEmpty())
    }

    @Test
    fun `start editing populates form and save updates existing entry`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            initialEntries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    name = "Breakfast",
                    amount = 300,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20540L
                )
            )
        )
        val viewModel = createViewModel(repository)

        runCurrent()
        viewModel.startEditing(repository.getEntries().single())
        assertEquals("Breakfast", viewModel.uiState.value.entryNameInput)
        viewModel.onEntryNameChanged("Cycling")
        viewModel.onCalorieInputChanged("450")
        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        viewModel.saveEntry()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.entries.size)
        assertEquals("Cycling", uiState.entries.single().name)
        assertEquals(450, uiState.entries.single().amount)
        assertEquals(CalorieEntryType.BURNED, uiState.entries.single().type)
        assertNull(uiState.editingEntryId)
    }

    @Test
    fun `delete entry removes it from overview and clears active edit state`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            initialEntries = listOf(
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

        runCurrent()
        viewModel.startEditing(repository.getEntries().single())
        viewModel.deleteEntry("entry-1")
        runCurrent()

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
        val repository = InMemoryCalorieEntryRepository(initialEntries = listOf(entry))
        val viewModel = createViewModel(repository)

        runCurrent()
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
        val repository = InMemoryCalorieEntryRepository(initialEntries = listOf(entry))
        val viewModel = createViewModel(repository)

        runCurrent()
        viewModel.requestDeleteEntry(entry)
        viewModel.confirmDeleteEntry()
        runCurrent()

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertNull(viewModel.uiState.value.pendingDeleteEntry)
    }

    @Test
    fun `select history filter narrows visible history days`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            initialEntries = listOf(
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

        runCurrent()
        assertEquals(1, viewModel.uiState.value.filteredHistoryDays.size)

        viewModel.selectHistoryFilter(HistoryFilter.AllTime)
        assertEquals(2, viewModel.uiState.value.filteredHistoryDays.size)

        viewModel.selectHistoryFilter(HistoryFilter.Today)
        assertEquals(1, viewModel.uiState.value.filteredHistoryDays.size)
        assertEquals(20540L, viewModel.uiState.value.filteredHistoryDays.single().epochDay)
    }

    @Test
    fun `select trend range expands the visible timeline`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            initialEntries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 120,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20510L
                ),
                CalorieEntry(
                    id = "entry-2",
                    amount = 240,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20540L
                )
            )
        )
        val viewModel = createViewModel(repository)

        runCurrent()
        assertEquals(30, viewModel.uiState.value.visibleTrendPoints.size)

        viewModel.selectTrendRange(TrendRange.AllTime)
        assertEquals(31, viewModel.uiState.value.visibleTrendPoints.size)

        viewModel.selectTrendRange(TrendRange.SevenDays)
        assertEquals(7, viewModel.uiState.value.visibleTrendPoints.size)
    }

    @Test
    fun `trend window navigation stays disabled when the timeline is empty`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.selectTrendRange(TrendRange.SevenDays)

        val initialState = viewModel.uiState.value
        assertTrue(initialState.visibleTrendPoints.isEmpty())
        assertFalse(initialState.canNavigateToEarlierTrendWindow)
        assertFalse(initialState.canNavigateToLaterTrendWindow)

        viewModel.showEarlierTrendWindow()
        viewModel.showLaterTrendWindow()

        val updatedState = viewModel.uiState.value
        assertTrue(updatedState.visibleTrendPoints.isEmpty())
        assertFalse(updatedState.canNavigateToEarlierTrendWindow)
        assertFalse(updatedState.canNavigateToLaterTrendWindow)
    }

    @Test
    fun `trend window navigation can move to earlier and later weeks`() = runTest {
        val repository = InMemoryCalorieEntryRepository(
            initialEntries = listOf(
                CalorieEntry(
                    id = "entry-1",
                    amount = 120,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20520L
                ),
                CalorieEntry(
                    id = "entry-2",
                    amount = 240,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = 20540L
                )
            )
        )
        val viewModel = createViewModel(repository)

        runCurrent()
        viewModel.selectTrendRange(TrendRange.SevenDays)
        assertEquals(20534L, viewModel.uiState.value.visibleTrendPoints.first().epochDay)
        assertTrue(viewModel.uiState.value.canNavigateToEarlierTrendWindow)
        assertFalse(viewModel.uiState.value.canNavigateToLaterTrendWindow)

        viewModel.showEarlierTrendWindow()
        assertEquals(20527L, viewModel.uiState.value.visibleTrendPoints.first().epochDay)
        assertTrue(viewModel.uiState.value.canNavigateToLaterTrendWindow)

        viewModel.showLaterTrendWindow()
        assertEquals(20534L, viewModel.uiState.value.visibleTrendPoints.first().epochDay)
    }

    @Test
    fun `selecting meal or activity source infers the matching calorie direction`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.onEntrySourceSelected(CalorieEntrySource.WATCH)
        assertEquals(CalorieEntryType.BURNED, viewModel.uiState.value.selectedType)

        viewModel.onEntrySourceSelected(CalorieEntrySource.MEAL)
        assertEquals(CalorieEntryType.INTAKE, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `manual source keeps the type picker visible`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        assertFalse(viewModel.uiState.value.showsManualTypePicker)

        viewModel.onEntrySourceSelected(CalorieEntrySource.MANUAL)
        assertTrue(viewModel.uiState.value.showsManualTypePicker)

        viewModel.onEntryTypeSelected(CalorieEntryType.BURNED)
        assertEquals(CalorieEntryType.BURNED, viewModel.uiState.value.selectedType)
        assertEquals(CalorieEntrySource.MANUAL, viewModel.uiState.value.selectedSource)
    }

    @Test
    fun `save goal target updates dashboard target and exits edit mode`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.startEditingGoalTarget()
        viewModel.onGoalTargetInputChanged("2600")
        viewModel.saveGoalTarget()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals(2600, uiState.targetCalories)
        assertEquals(2600, uiState.goalProgressInsights?.targetCalories)
        assertFalse(uiState.isEditingGoalTarget)
        assertNull(uiState.goalTargetError)
    }

    @Test
    fun `save goal target with invalid input exposes validation error`() = runTest {
        val viewModel = createViewModel()

        runCurrent()
        viewModel.startEditingGoalTarget()
        viewModel.onGoalTargetInputChanged("abc")
        viewModel.saveGoalTarget()
        runCurrent()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isEditingGoalTarget)
        assertEquals(GoalTargetValidationError.NotWholeNumber, uiState.goalTargetError)
        assertEquals(2200, uiState.targetCalories)
    }

    private fun createViewModel(
        repository: CalorieEntryRepository = InMemoryCalorieEntryRepository(),
        goalTargetRepository: GoalTargetRepository = InMemoryGoalTargetRepository()
    ): TrackerViewModel {
        val clock = fixedTestClock()
        return TrackerViewModel(
            saveCalorieEntryUseCase = SaveCalorieEntryUseCase(
                repository = repository,
                inputValidator = CalorieInputValidator(),
                clock = clock
            ),
            deleteCalorieEntryUseCase = DeleteCalorieEntryUseCase(
                repository = repository
            ),
            loadCalorieHistoryUseCase = LoadCalorieHistoryUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator()
            ),
            loadCalorieTimelineTrendUseCase = LoadCalorieTimelineTrendUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator(),
                clock = clock
            ),
            loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator(),
                clock = clock
            ),
            loadWeeklyCalorieTrendUseCase = LoadWeeklyCalorieTrendUseCase(
                repository = repository,
                dailyCalorieCalculator = DailyCalorieCalculator(),
                clock = clock
            ),
            loadGoalTargetUseCase = LoadGoalTargetUseCase(
                repository = goalTargetRepository
            ),
            updateGoalTargetUseCase = UpdateGoalTargetUseCase(
                repository = goalTargetRepository
            ),
            calculateGoalProgressUseCase = CalculateGoalProgressUseCase(),
            portionCalorieCalculator = PortionCalorieCalculator(
                inputValidator = CalorieInputValidator()
            ),
            clock = clock,
            dayTickerFlow = flowOf(LocalDate.now(clock).toEpochDay())
        )
    }
}
