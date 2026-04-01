package com.example.kalorientracker.ui.tracker

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieInputValidationError
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieHistoryDay
import com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint
import com.example.kalorientracker.domain.calorie.GoalProgressInsights
import com.example.kalorientracker.domain.calorie.GoalTargetValidationError
import com.example.kalorientracker.domain.calorie.SupportedAiModel

data class TrackerUiState(
    val selectedDestination: TrackerDestination = TrackerDestination.Overview,
    val dayNumber: Int = 1,
    val entries: List<CalorieEntry> = emptyList(),
    val historyDays: List<CalorieHistoryDay> = emptyList(),
    val weeklyTrend: List<DailyCalorieTrendPoint> = emptyList(),
    val timelineTrend: List<DailyCalorieTrendPoint> = emptyList(),
    val goalProgressInsights: GoalProgressInsights? = null,
    val targetCalories: Int = 2200,
    val targetCaloriesInput: String = "",
    val isEditingGoalTarget: Boolean = false,
    val isDatePickerVisible: Boolean = false,
    val currentEpochDay: Long = 0,
    val entryRecordedOnEpochDay: Long = 0,
    val selectedTrendRange: TrendRange = TrendRange.ThirtyDays,
    val selectedTrendWindowEndEpochDay: Long? = null,
    val selectedHistoryFilter: HistoryFilter = HistoryFilter.SevenDays,
    val pendingDeleteEntry: CalorieEntry? = null,
    val entryNameInput: String = "",
    val entryInputMode: EntryInputMode = EntryInputMode.DirectCalories,
    val calorieInput: String = "",
    val consumedAmountInput: String = "",
    val caloriesPer100Input: String = "",
    val selectedType: CalorieEntryType = CalorieEntryType.INTAKE,
    val selectedSource: CalorieEntrySource = CalorieEntrySource.MEAL,
    val editingEntryId: String? = null,
    val inputError: CalorieInputValidationError? = null,
    val consumedAmountInputError: CalorieInputValidationError? = null,
    val caloriesPer100InputError: CalorieInputValidationError? = null,
    val goalTargetError: GoalTargetValidationError? = null,
    val aiMealDescriptionInput: String = "",
    val selectedAiModel: SupportedAiModel = SupportedAiModel.GEMINI_1_5_FLASH,
    val isAiAnalyzing: Boolean = false,
    val aiAnalysisError: String? = null,
    val totalIntake: Int = 0,
    val totalBurned: Int = 0,
    val netCalories: Int = 0
) {
    val hasEntries: Boolean
        get() = entries.isNotEmpty()

    val calculatedCaloriesPreview: Int?
        get() {
            val consumedAmount = consumedAmountInput.toDoubleOrNull()
            val caloriesPer100 = caloriesPer100Input.toDoubleOrNull()
            if (consumedAmount == null || caloriesPer100 == null || consumedAmount <= 0.0 || caloriesPer100 <= 0.0) {
                return null
            }

            return kotlin.math.round(consumedAmount * caloriesPer100 / 100.0).toInt()
        }

    val hasHistory: Boolean
        get() = filteredHistoryDays.isNotEmpty()

    val isEditing: Boolean
        get() = editingEntryId != null

    val canMoveEntryDateForward: Boolean
        get() = entryRecordedOnEpochDay < currentEpochDay

    val showsManualTypePicker: Boolean
        get() = selectedSource == CalorieEntrySource.MANUAL

    val showsMagicInput: Boolean
        get() = usesDirectCalorieInput && !isEditing

    val usesDirectCalorieInput: Boolean
        get() = entryInputMode == EntryInputMode.DirectCalories

    val usesPortionCalculator: Boolean
        get() = entryInputMode == EntryInputMode.PortionCalculator

    val visibleTrendWindowDays: Int?
        get() = when (selectedTrendRange) {
            TrendRange.SevenDays -> 7
            TrendRange.ThirtyDays -> 30
            TrendRange.AllTime -> null
        }

    val effectiveTrendWindowEndEpochDay: Long
        get() = (selectedTrendWindowEndEpochDay ?: currentEpochDay).coerceAtMost(currentEpochDay)

    val visibleTrendPoints: List<DailyCalorieTrendPoint>
        get() = when (selectedTrendRange) {
            TrendRange.SevenDays,
            TrendRange.ThirtyDays -> {
                val windowDays = visibleTrendWindowDays ?: return emptyList()
                val startEpochDay = effectiveTrendWindowEndEpochDay - windowDays + 1L
                timelineTrend.filter { it.epochDay in startEpochDay..effectiveTrendWindowEndEpochDay }
            }
            TrendRange.AllTime -> timelineTrend
        }

    val canNavigateToEarlierTrendWindow: Boolean
        get() {
            val earliestTimelineEpochDay = timelineTrend.firstOrNull()?.epochDay ?: return false
            return visibleTrendWindowDays != null &&
                visibleTrendPoints.isNotEmpty() &&
                visibleTrendPoints.first().epochDay > earliestTimelineEpochDay
        }

    val canNavigateToLaterTrendWindow: Boolean
        get() = visibleTrendWindowDays != null &&
            visibleTrendPoints.isNotEmpty() &&
            visibleTrendPoints.last().epochDay < currentEpochDay

    val filteredHistoryDays: List<CalorieHistoryDay>
        get() = when (selectedHistoryFilter) {
            HistoryFilter.Today -> historyDays.filter { it.epochDay == currentEpochDay }
            HistoryFilter.SevenDays -> historyDays.filter { it.epochDay >= currentEpochDay - 6L }
            HistoryFilter.AllTime -> historyDays
        }
}

enum class TrackerDestination {
    Overview,
    Capture,
    History
}

enum class HistoryFilter {
    Today,
    SevenDays,
    AllTime
}

enum class TrendRange {
    SevenDays,
    ThirtyDays,
    AllTime
}

enum class EntryInputMode {
    DirectCalories,
    PortionCalculator
}
