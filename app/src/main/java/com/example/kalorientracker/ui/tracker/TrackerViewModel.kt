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
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieHistoryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.LoadWeeklyCalorieTrendUseCase
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryResult
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
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
    private val loadCalorieOverviewUseCase: LoadCalorieOverviewUseCase,
    private val loadWeeklyCalorieTrendUseCase: LoadWeeklyCalorieTrendUseCase,
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

    fun onEntryTypeSelected(type: CalorieEntryType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onEntrySourceSelected(source: CalorieEntrySource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    fun saveEntry() {
        val currentState = _uiState.value
        viewModelScope.launch {
            when (
                val result = saveCalorieEntryUseCase(
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
                calorieInput = entry.amount.toString(),
                selectedType = entry.type,
                selectedSource = entry.source,
                editingEntryId = entry.id,
                editingEntryRecordedOnEpochDay = entry.recordedOnEpochDay,
                inputError = null
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
        val historyDays = loadCalorieHistoryUseCase()
        val overview = loadCalorieOverviewUseCase()
        val weeklyTrend = loadWeeklyCalorieTrendUseCase()
        _uiState.update {
            it.copy(
                entries = overview.entries,
                historyDays = historyDays,
                weeklyTrend = weeklyTrend,
                currentEpochDay = LocalDate.now(clock).toEpochDay(),
                totalIntake = overview.summary.totalIntake,
                totalBurned = overview.summary.totalBurned,
                netCalories = overview.summary.netCalories
            )
        }
    }

    private fun clearEntryEditor() {
        _uiState.update {
            it.copy(
                calorieInput = "",
                selectedType = CalorieEntryType.INTAKE,
                selectedSource = CalorieEntrySource.MANUAL,
                editingEntryId = null,
                editingEntryRecordedOnEpochDay = null,
                inputError = null
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    CalorieTrackerDatabase::class.java,
                    TRACKER_DATABASE
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
                    loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                        repository = repository,
                        dailyCalorieCalculator = DailyCalorieCalculator()
                    ),
                    loadWeeklyCalorieTrendUseCase = LoadWeeklyCalorieTrendUseCase(
                        repository = repository,
                        dailyCalorieCalculator = DailyCalorieCalculator(),
                        clock = Clock.systemDefaultZone()
                    ),
                    clock = Clock.systemDefaultZone()
                )
            }
        }

        private const val TRACKER_DATABASE = "calorie_tracker.db"
        private const val LEGACY_TRACKER_PREFERENCES = "calorie_tracker_preferences"
    }
}
