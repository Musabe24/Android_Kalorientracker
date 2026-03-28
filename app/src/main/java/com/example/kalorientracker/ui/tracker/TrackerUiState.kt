package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType

data class TrackerUiState(
    val dayNumber: Int = 1,
    val entries: List<CalorieEntry> = emptyList(),
    val calorieInput: String = "",
    val selectedType: CalorieEntryType = CalorieEntryType.INTAKE,
    val selectedSource: CalorieEntrySource = CalorieEntrySource.MANUAL,
    val editingEntryId: String? = null,
    val editingEntryRecordedOnEpochDay: Long? = null,
    val inputError: String? = null,
    val totalIntake: Int = 0,
    val totalBurned: Int = 0,
    val netCalories: Int = 0
) {
    val hasEntries: Boolean
        get() = entries.isNotEmpty()

    val isEditing: Boolean
        get() = editingEntryId != null
}
