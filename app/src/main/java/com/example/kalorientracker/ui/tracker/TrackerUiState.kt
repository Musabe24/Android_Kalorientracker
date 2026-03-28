package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieHistoryDay
import com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint

data class TrackerUiState(
    val dayNumber: Int = 1,
    val entries: List<CalorieEntry> = emptyList(),
    val historyDays: List<CalorieHistoryDay> = emptyList(),
    val weeklyTrend: List<DailyCalorieTrendPoint> = emptyList(),
    val currentEpochDay: Long = 0,
    val selectedHistoryFilter: HistoryFilter = HistoryFilter.SevenDays,
    val pendingDeleteEntry: CalorieEntry? = null,
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

    val hasHistory: Boolean
        get() = filteredHistoryDays.isNotEmpty()

    val isEditing: Boolean
        get() = editingEntryId != null

    val filteredHistoryDays: List<CalorieHistoryDay>
        get() = when (selectedHistoryFilter) {
            HistoryFilter.Today -> historyDays.filter { it.epochDay == currentEpochDay }
            HistoryFilter.SevenDays -> historyDays.filter { it.epochDay >= currentEpochDay - 6L }
            HistoryFilter.AllTime -> historyDays
        }
}

enum class HistoryFilter {
    Today,
    SevenDays,
    AllTime
}
