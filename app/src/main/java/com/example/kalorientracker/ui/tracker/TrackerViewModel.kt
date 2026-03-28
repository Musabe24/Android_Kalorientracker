package com.example.kalorientracker.ui.tracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kalorientracker.data.calorie.CalorieEntryStorageCodec
import com.example.kalorientracker.data.calorie.SharedPreferencesCalorieEntryRepository
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.DeleteCalorieEntryUseCase
import com.example.kalorientracker.domain.calorie.LoadCalorieOverviewUseCase
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryResult
import com.example.kalorientracker.domain.calorie.SaveCalorieEntryUseCase
import java.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackerViewModel(
    private val saveCalorieEntryUseCase: SaveCalorieEntryUseCase,
    private val deleteCalorieEntryUseCase: DeleteCalorieEntryUseCase,
    private val loadCalorieOverviewUseCase: LoadCalorieOverviewUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        refreshOverview()
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
        deleteCalorieEntryUseCase(entryId)
        refreshOverview()
        if (_uiState.value.editingEntryId == entryId) {
            clearEntryEditor()
        }
    }

    private fun refreshOverview() {
        val overview = loadCalorieOverviewUseCase()
        _uiState.update {
            it.copy(
                entries = overview.entries,
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
                val repository = SharedPreferencesCalorieEntryRepository(
                    sharedPreferences = context.applicationContext.getSharedPreferences(
                        TRACKER_PREFERENCES,
                        Context.MODE_PRIVATE
                    ),
                    storageCodec = CalorieEntryStorageCodec()
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
                    loadCalorieOverviewUseCase = LoadCalorieOverviewUseCase(
                        repository = repository,
                        dailyCalorieCalculator = DailyCalorieCalculator()
                    )
                )
            }
        }

        private const val TRACKER_PREFERENCES = "calorie_tracker_preferences"
    }
}
