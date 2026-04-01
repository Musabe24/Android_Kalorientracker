package com.example.kalorientracker.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kalorientracker.app.TrackerAppContainer
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.GoalProgressInsights
import com.example.kalorientracker.domain.calorie.GoalTargetValidationError
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieTimelineTrendUseCase
import com.example.kalorientracker.domain.calorie.LoadGoalTargetUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.PortionCalorieCalculationResult
import com.example.kalorientracker.domain.calorie.PortionCalorieCalculator
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryResult
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetResult
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val saveCalorieEntryUseCase: SaveCalorieEntryUseCase,
    private val deleteCalorieEntryUseCase: DeleteCalorieEntryUseCase,
    private val loadCalorieHistoryUseCase: LoadCalorieHistoryUseCase,
    private val loadCalorieTimelineTrendUseCase: LoadCalorieTimelineTrendUseCase,
    private val loadCalorieOverviewUseCase: LoadCalorieOverviewUseCase,
    private val loadWeeklyCalorieTrendUseCase: LoadWeeklyCalorieTrendUseCase,
    private val loadGoalTargetUseCase: LoadGoalTargetUseCase,
    private val updateGoalTargetUseCase: UpdateGoalTargetUseCase,
    private val calculateGoalProgressUseCase: CalculateGoalProgressUseCase,
    private val portionCalorieCalculator: PortionCalorieCalculator,
    private val clock: Clock,
    dayTickerFlow: kotlinx.coroutines.flow.Flow<Long>? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TrackerUiState(
            currentEpochDay = LocalDate.now(clock).toEpochDay(),
            entryRecordedOnEpochDay = LocalDate.now(clock).toEpochDay()
        )
    )
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val dayTicker = dayTickerFlow ?: flow {
        while (true) {
            emit(LocalDate.now(clock).toEpochDay())
            delay(60_000) // Check every minute
        }
    }.distinctUntilChanged()

    init {
        viewModelScope.launch {
            combine(
                loadCalorieOverviewUseCase.observe(),
                loadCalorieHistoryUseCase.observe(),
                loadCalorieTimelineTrendUseCase.observe(),
                loadWeeklyCalorieTrendUseCase.observe(),
                loadGoalTargetUseCase.observe(),
                dayTicker
            ) { args: Array<Any> ->
                val overview = args[0] as com.example.kalorientracker.domain.calorie.CalorieOverview
                val historyDays = args[1] as List<com.example.kalorientracker.domain.calorie.CalorieHistoryDay>
                val timelineTrend = args[2] as List<com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint>
                val weeklyTrend = args[3] as List<com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint>
                val targetCalories = args[4] as Int
                val currentEpochDay = args[5] as Long

                TrackerObservedState(
                    overview = overview,
                    historyDays = historyDays,
                    timelineTrend = timelineTrend,
                    weeklyTrend = weeklyTrend,
                    targetCalories = targetCalories,
                    goalProgressInsights = calculateGoalProgressUseCase(
                        netCalories = overview.summary.netCalories,
                        weeklyTrend = weeklyTrend,
                        targetCalories = targetCalories
                    ),
                    currentEpochDay = currentEpochDay
                )
            }.collect { observedState ->
                _uiState.update { currentState ->
                    currentState.copy(
                        dayNumber = LocalDate.ofEpochDay(observedState.currentEpochDay).dayOfMonth,
                        entries = observedState.overview.entries,
                        historyDays = observedState.historyDays,
                        timelineTrend = observedState.timelineTrend,
                        weeklyTrend = observedState.weeklyTrend,
                        goalProgressInsights = observedState.goalProgressInsights,
                        targetCalories = observedState.targetCalories,
                        currentEpochDay = observedState.currentEpochDay,
                        selectedTrendWindowEndEpochDay = currentState.selectedTrendWindowEndEpochDay
                            ?.coerceAtMost(observedState.currentEpochDay)
                            ?: observedState.currentEpochDay,
                        entryRecordedOnEpochDay = currentState.entryRecordedOnEpochDay
                            .coerceAtMost(observedState.currentEpochDay),
                        totalIntake = observedState.overview.summary.totalIntake,
                        totalBurned = observedState.overview.summary.totalBurned,
                        netCalories = observedState.overview.summary.netCalories
                    )
                }
            }
        }
    }

    fun onCalorieInputChanged(value: String) {
        _uiState.update {
            it.copy(
                calorieInput = value,
                inputError = null
            )
        }
    }

    fun onEntryInputModeSelected(mode: EntryInputMode) {
        _uiState.update { currentState ->
            currentState.copy(
                entryInputMode = mode,
                inputError = null,
                consumedAmountInputError = null,
                caloriesPer100InputError = null
            )
        }
    }

    fun onConsumedAmountInputChanged(value: String) {
        _uiState.update {
            it.copy(
                consumedAmountInput = value,
                consumedAmountInputError = null,
                caloriesPer100InputError = null
            )
        }
    }

    fun onCaloriesPer100InputChanged(value: String) {
        _uiState.update {
            it.copy(
                caloriesPer100Input = value,
                consumedAmountInputError = null,
                caloriesPer100InputError = null
            )
        }
    }

    fun onEntryNameChanged(value: String) {
        _uiState.update { it.copy(entryNameInput = value) }
    }

    fun selectDestination(destination: TrackerDestination) {
        _uiState.update { it.copy(selectedDestination = destination) }
    }

    fun onEntryTypeSelected(type: CalorieEntryType) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedType = type,
                selectedSource = if (currentState.selectedSource == CalorieEntrySource.MANUAL) {
                    CalorieEntrySource.MANUAL
                } else {
                    inferredSourceForType(type)
                }
            )
        }
    }

    fun onEntrySourceSelected(source: CalorieEntrySource) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedSource = source,
                selectedType = inferredTypeForSource(source) ?: currentState.selectedType
            )
        }
    }

    fun onEntryDateSelected(epochDay: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                entryRecordedOnEpochDay = epochDay.coerceAtMost(currentState.currentEpochDay)
            )
        }
    }

    fun showPreviousEntryDate() {
        _uiState.update { currentState ->
            currentState.copy(entryRecordedOnEpochDay = currentState.entryRecordedOnEpochDay - 1L)
        }
    }

    fun showNextEntryDate() {
        _uiState.update { currentState ->
            currentState.copy(
                entryRecordedOnEpochDay = (currentState.entryRecordedOnEpochDay + 1L)
                    .coerceAtMost(currentState.currentEpochDay)
            )
        }
    }

    fun resetEntryDateToToday() {
        _uiState.update { currentState ->
            currentState.copy(entryRecordedOnEpochDay = currentState.currentEpochDay)
        }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(isDatePickerVisible = true) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(isDatePickerVisible = false) }
    }

    fun startEditingGoalTarget() {
        _uiState.update {
            it.copy(
                selectedDestination = TrackerDestination.Overview,
                isEditingGoalTarget = true,
                targetCaloriesInput = it.targetCalories.toString(),
                goalTargetError = null
            )
        }
    }

    fun onGoalTargetInputChanged(value: String) {
        _uiState.update {
            it.copy(
                targetCaloriesInput = value,
                goalTargetError = null
            )
        }
    }

    fun cancelGoalTargetEditing() {
        _uiState.update {
            it.copy(
                isEditingGoalTarget = false,
                targetCaloriesInput = "",
                goalTargetError = null
            )
        }
    }

    fun saveGoalTarget() {
        val currentInput = _uiState.value.targetCaloriesInput
        viewModelScope.launch {
            when (val result = updateGoalTargetUseCase(currentInput)) {
                is UpdateGoalTargetResult.ValidationError -> {
                    _uiState.update { it.copy(goalTargetError = result.reason) }
                }

                is UpdateGoalTargetResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isEditingGoalTarget = false,
                            targetCaloriesInput = "",
                            goalTargetError = null
                        )
                    }
                }
            }
        }
    }

    fun saveEntry() {
        val currentState = _uiState.value
        val rawCalories = when (currentState.entryInputMode) {
            EntryInputMode.DirectCalories -> currentState.calorieInput
            EntryInputMode.PortionCalculator -> {
                when (
                    val calculationResult = portionCalorieCalculator.calculate(
                        rawConsumedAmount = currentState.consumedAmountInput,
                        rawCaloriesPer100Units = currentState.caloriesPer100Input
                    )
                ) {
                    is PortionCalorieCalculationResult.Invalid -> {
                        _uiState.update {
                            it.copy(
                                inputError = null,
                                consumedAmountInputError = calculationResult.errors.consumedAmountError,
                                caloriesPer100InputError = calculationResult.errors.caloriesPer100UnitsError
                            )
                        }
                        return
                    }

                    is PortionCalorieCalculationResult.Valid -> {
                        calculationResult.calories.toString()
                    }
                }
            }
        }

        viewModelScope.launch {
            when (
                val result = saveCalorieEntryUseCase(
                    rawName = currentState.entryNameInput,
                    rawCalories = rawCalories,
                    entryType = currentState.selectedType,
                    entrySource = currentState.selectedSource,
                    editingEntryId = currentState.editingEntryId,
                    recordedOnEpochDay = currentState.entryRecordedOnEpochDay
                )
            ) {
                is SaveCalorieEntryResult.ValidationError -> {
                    _uiState.update { it.copy(inputError = result.reason) }
                }

                SaveCalorieEntryResult.Success -> {
                    clearEntryEditor()
                }
            }
        }
    }

    fun startEditing(entry: CalorieEntry) {
        _uiState.update {
            it.copy(
                selectedDestination = TrackerDestination.Capture,
                entryNameInput = entry.name,
                entryInputMode = EntryInputMode.DirectCalories,
                calorieInput = entry.amount.toString(),
                consumedAmountInput = "",
                caloriesPer100Input = "",
                selectedType = entry.type,
                selectedSource = entry.source,
                editingEntryId = entry.id,
                entryRecordedOnEpochDay = entry.recordedOnEpochDay,
                inputError = null,
                consumedAmountInputError = null,
                caloriesPer100InputError = null,
                pendingDeleteEntry = null
            )
        }
    }

    fun cancelEditing() {
        clearEntryEditor()
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            deleteCalorieEntryUseCase(entryId)
            if (_uiState.value.editingEntryId == entryId) {
                clearEntryEditor()
            }
        }
    }

    fun selectHistoryFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(selectedHistoryFilter = filter) }
    }

    fun selectTrendRange(range: TrendRange) {
        _uiState.update {
            it.copy(
                selectedTrendRange = range,
                selectedTrendWindowEndEpochDay = it.currentEpochDay
            )
        }
    }

    fun showEarlierTrendWindow() {
        _uiState.update { currentState ->
            val windowDays = currentState.visibleTrendWindowDays ?: return@update currentState
            val earliestEpochDay = currentState.timelineTrend.firstOrNull()?.epochDay ?: return@update currentState
            val nextEndEpochDay = (currentState.effectiveTrendWindowEndEpochDay - windowDays)
                .coerceAtLeast(earliestEpochDay + windowDays - 1L)

            currentState.copy(selectedTrendWindowEndEpochDay = nextEndEpochDay)
        }
    }

    fun showLaterTrendWindow() {
        _uiState.update { currentState ->
            val windowDays = currentState.visibleTrendWindowDays ?: return@update currentState
            val nextEndEpochDay = (currentState.effectiveTrendWindowEndEpochDay + windowDays)
                .coerceAtMost(currentState.currentEpochDay)

            currentState.copy(selectedTrendWindowEndEpochDay = nextEndEpochDay)
        }
    }

    fun requestDeleteEntry(entry: CalorieEntry) {
        _uiState.update { it.copy(pendingDeleteEntry = entry) }
    }

    fun dismissDeleteEntry() {
        _uiState.update { it.copy(pendingDeleteEntry = null) }
    }

    fun confirmDeleteEntry() {
        val pendingEntry = _uiState.value.pendingDeleteEntry ?: return
        dismissDeleteEntry()
        deleteEntry(pendingEntry.id)
    }

    private fun clearEntryEditor() {
        _uiState.update {
            it.copy(
                entryNameInput = "",
                entryInputMode = EntryInputMode.DirectCalories,
                calorieInput = "",
                consumedAmountInput = "",
                caloriesPer100Input = "",
                selectedType = CalorieEntryType.INTAKE,
                selectedSource = CalorieEntrySource.MEAL,
                editingEntryId = null,
                entryRecordedOnEpochDay = it.currentEpochDay,
                inputError = null,
                consumedAmountInputError = null,
                caloriesPer100InputError = null
            )
        }
    }

    private fun inferredTypeForSource(source: CalorieEntrySource): CalorieEntryType? {
        return when (source) {
            CalorieEntrySource.MEAL -> CalorieEntryType.INTAKE
            CalorieEntrySource.WATCH -> CalorieEntryType.BURNED
            CalorieEntrySource.MANUAL -> null
        }
    }

    private fun inferredSourceForType(type: CalorieEntryType): CalorieEntrySource {
        return when (type) {
            CalorieEntryType.INTAKE -> CalorieEntrySource.MEAL
            CalorieEntryType.BURNED -> CalorieEntrySource.WATCH
        }
    }

    companion object {
        fun factory(appContainer: TrackerAppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TrackerViewModel(
                    saveCalorieEntryUseCase = appContainer.saveCalorieEntryUseCase,
                    deleteCalorieEntryUseCase = appContainer.deleteCalorieEntryUseCase,
                    loadCalorieHistoryUseCase = appContainer.loadCalorieHistoryUseCase,
                    loadCalorieTimelineTrendUseCase = appContainer.loadCalorieTimelineTrendUseCase,
                    loadCalorieOverviewUseCase = appContainer.loadCalorieOverviewUseCase,
                    loadWeeklyCalorieTrendUseCase = appContainer.loadWeeklyCalorieTrendUseCase,
                    loadGoalTargetUseCase = appContainer.loadGoalTargetUseCase,
                    updateGoalTargetUseCase = appContainer.updateGoalTargetUseCase,
                    calculateGoalProgressUseCase = appContainer.calculateGoalProgressUseCase,
                    portionCalorieCalculator = appContainer.portionCalorieCalculator,
                    clock = appContainer.clock
                )
            }
        }
    }
}

private data class TrackerObservedState(
    val overview: com.example.kalorientracker.domain.calorie.CalorieOverview,
    val historyDays: List<com.example.kalorientracker.domain.calorie.CalorieHistoryDay>,
    val timelineTrend: List<com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint>,
    val weeklyTrend: List<com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint>,
    val targetCalories: Int,
    val goalProgressInsights: GoalProgressInsights,
    val currentEpochDay: Long
)
