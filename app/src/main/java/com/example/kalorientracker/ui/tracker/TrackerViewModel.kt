package com.example.kalorientracker.ui.tracker

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kalorientracker.data.calorie.CalorieTrackerDatabase
import com.example.kalorientracker.data.calorie.CalorieEntryStorageCodec
import com.example.kalorientracker.data.calorie.LegacyCalorieEntryStore
import com.example.kalorientracker.data.calorie.RoomCalorieEntryRepository
import com.example.kalorientracker.data.calorie.RoomGoalTargetRepository
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadGoalTargetUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieTimelineTrendUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryResult
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetResult
import com.example.kalorientracker.domain.calorie.UpdateGoalTargetUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val clock: Clock
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TrackerUiState(currentEpochDay = LocalDate.now(clock).toEpochDay())
    )
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshOverview()
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

    fun onEntryNameChanged(value: String) {
        _uiState.update { it.copy(entryNameInput = value) }
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

    fun startEditingGoalTarget() {
        _uiState.update {
            it.copy(
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
                    _uiState.update { it.copy(goalTargetError = result.message) }
                }

                is UpdateGoalTargetResult.Success -> {
                    refreshOverview()
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
        viewModelScope.launch {
            when (
                val result = saveCalorieEntryUseCase(
                    rawName = currentState.entryNameInput,
                    rawCalories = currentState.calorieInput,
                    entryType = currentState.selectedType,
                    entrySource = currentState.selectedSource,
                    editingEntryId = currentState.editingEntryId,
                    editingEntryRecordedOnEpochDay = currentState.editingEntryRecordedOnEpochDay
                )
            ) {
                is SaveCalorieEntryResult.ValidationError -> {
                    _uiState.update { it.copy(inputError = result.message) }
                }

                SaveCalorieEntryResult.Success -> {
                    refreshOverview()
                    clearEntryEditor()
                }
            }
        }
    }

    fun startEditing(entry: CalorieEntry) {
        _uiState.update {
            it.copy(
                entryNameInput = entry.name,
                calorieInput = entry.amount.toString(),
                selectedType = entry.type,
                selectedSource = entry.source,
                editingEntryId = entry.id,
                editingEntryRecordedOnEpochDay = entry.recordedOnEpochDay,
                inputError = null,
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
            refreshOverview()
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

    private suspend fun refreshOverview() {
        val targetCalories = loadGoalTargetUseCase()
        val historyDays = loadCalorieHistoryUseCase()
        val timelineTrend = loadCalorieTimelineTrendUseCase()
        val overview = loadCalorieOverviewUseCase()
        val weeklyTrend = loadWeeklyCalorieTrendUseCase()
        val goalProgressInsights = calculateGoalProgressUseCase(
            netCalories = overview.summary.netCalories,
            weeklyTrend = weeklyTrend,
            targetCalories = targetCalories
        )
        _uiState.update {
            it.copy(
                entries = overview.entries,
                historyDays = historyDays,
                timelineTrend = timelineTrend,
                weeklyTrend = weeklyTrend,
                goalProgressInsights = goalProgressInsights,
                targetCalories = targetCalories,
                currentEpochDay = LocalDate.now(clock).toEpochDay(),
                selectedTrendWindowEndEpochDay = it.selectedTrendWindowEndEpochDay
                    ?: LocalDate.now(clock).toEpochDay(),
                totalIntake = overview.summary.totalIntake,
                totalBurned = overview.summary.totalBurned,
                netCalories = overview.summary.netCalories
            )
        }
    }

    private fun clearEntryEditor() {
        _uiState.update {
            it.copy(
                entryNameInput = "",
                calorieInput = "",
                selectedType = CalorieEntryType.INTAKE,
                selectedSource = CalorieEntrySource.MEAL,
                editingEntryId = null,
                editingEntryRecordedOnEpochDay = null,
                inputError = null
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
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    CalorieTrackerDatabase::class.java,
                    TRACKER_DATABASE
                ).addMigrations(
                    CalorieTrackerDatabase.Migration1To2,
                    CalorieTrackerDatabase.Migration2To3
                ).build()
                val repository = RoomCalorieEntryRepository(
                    calorieEntryDao = database.calorieEntryDao(),
                    legacyCalorieEntryStore = LegacyCalorieEntryStore(
                        sharedPreferences = context.applicationContext.getSharedPreferences(
                            LEGACY_TRACKER_PREFERENCES,
                            Context.MODE_PRIVATE
                        ),
                        storageCodec = CalorieEntryStorageCodec()
                    )
                )
                val goalTargetRepository = RoomGoalTargetRepository(
                    goalSettingsDao = database.goalSettingsDao()
                )
                TrackerViewModel(
                    saveCalorieEntryUseCase = SaveCalorieEntryUseCase(
                        repository = repository,
                        inputValidator = CalorieInputValidator(),
                        clock = Clock.systemDefaultZone()
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
                        clock = Clock.systemDefaultZone()
                    ),
                    loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                        repository = repository,
                        dailyCalorieCalculator = DailyCalorieCalculator()
                    ),
                    loadWeeklyCalorieTrendUseCase = LoadWeeklyCalorieTrendUseCase(
                        repository = repository,
                        dailyCalorieCalculator = DailyCalorieCalculator(),
                        clock = Clock.systemDefaultZone()
                    ),
                    loadGoalTargetUseCase = LoadGoalTargetUseCase(
                        repository = goalTargetRepository
                    ),
                    updateGoalTargetUseCase = UpdateGoalTargetUseCase(
                        repository = goalTargetRepository
                    ),
                    calculateGoalProgressUseCase = CalculateGoalProgressUseCase(),
                    clock = Clock.systemDefaultZone()
                )
            }
        }

        private const val TRACKER_DATABASE = "calorie_tracker.db"
        private const val LEGACY_TRACKER_PREFERENCES = "calorie_tracker_preferences"
    }
}
