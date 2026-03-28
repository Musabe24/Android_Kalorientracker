package com.example.kalorientracker.ui.home

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType

data class GreetingUiState(
    val entries: List<CalorieEntry> = emptyList(),
    val calorieInput: String = "",
    val selectedType: CalorieEntryType = CalorieEntryType.INTAKE,
    val selectedSource: CalorieEntrySource = CalorieEntrySource.MANUAL,
    val inputError: String? = null,
    val totalIntake: Int = 0,
    val totalBurned: Int = 0,
    val netCalories: Int = 0
)
