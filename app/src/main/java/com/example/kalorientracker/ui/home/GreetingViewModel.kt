package com.example.kalorientracker.ui.home

import androidx.lifecycle.ViewModel
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidator
import com.example.kalorientracker.domain.calorie.DailyCalorieCalculator
import com.example.kalorientracker.domain.calorie.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GreetingViewModel(
    private val inputValidator: CalorieInputValidator = CalorieInputValidator(),
    private val dailyCalorieCalculator: DailyCalorieCalculator = DailyCalorieCalculator()
) : ViewModel() {
    private val _uiState = MutableStateFlow(GreetingUiState())
    val uiState: StateFlow<GreetingUiState> = _uiState.asStateFlow()

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

    fun addEntry() {
        when (val validationResult = inputValidator.validate(_uiState.value.calorieInput)) {
            is ValidationResult.Invalid -> {
                _uiState.update { it.copy(inputError = validationResult.reason) }
            }
            is ValidationResult.Valid -> {
                val newEntry = CalorieEntry(
                    amount = validationResult.calories,
                    type = _uiState.value.selectedType,
                    source = _uiState.value.selectedSource
                )

                val updatedEntries = _uiState.value.entries + newEntry
                val summary = dailyCalorieCalculator.calculateSummary(updatedEntries)

                _uiState.update {
                    it.copy(
                        entries = updatedEntries,
                        calorieInput = "",
                        inputError = null,
                        totalIntake = summary.totalIntake,
                        totalBurned = summary.totalBurned,
                        netCalories = summary.netCalories
                    )
                }
            }
        }
    }
}
