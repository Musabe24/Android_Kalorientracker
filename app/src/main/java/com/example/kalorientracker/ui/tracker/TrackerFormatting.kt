package com.example.kalorientracker.ui.tracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidationError
import com.example.kalorientracker.domain.calorie.GoalTargetValidationError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun calorieInputErrorMessage(error: CalorieInputValidationError?): String? {
    return when (error) {
        CalorieInputValidationError.Blank -> stringResource(R.string.error_input_blank)
        CalorieInputValidationError.NotWholeNumber -> stringResource(R.string.error_input_not_whole_number)
        CalorieInputValidationError.NonPositive -> stringResource(R.string.error_input_non_positive)
        null -> null
    }
}

@Composable
internal fun goalTargetErrorMessage(error: GoalTargetValidationError?): String? {
    return when (error) {
        GoalTargetValidationError.Blank -> stringResource(R.string.error_target_blank)
        GoalTargetValidationError.NotWholeNumber -> stringResource(R.string.error_target_not_whole_number)
        GoalTargetValidationError.NonPositive -> stringResource(R.string.error_target_non_positive)
        null -> null
    }
}

@Composable
internal fun sourceLabel(source: CalorieEntrySource): String {
    return when (source) {
        CalorieEntrySource.MEAL -> stringResource(R.string.entry_source_meal)
        CalorieEntrySource.WATCH -> stringResource(R.string.entry_source_watch)
        CalorieEntrySource.MANUAL -> stringResource(R.string.entry_source_manual)
    }
}

@Composable
internal fun entryDisplayTitle(entry: CalorieEntry, index: Int): String {
    if (entry.name.isNotBlank()) {
        return entry.name
    }

    return stringResource(
        if (entry.type == CalorieEntryType.INTAKE) {
            R.string.history_intake_entry_template
        } else {
            R.string.history_burned_entry_template
        },
        index
    )
}

@Composable
internal fun entryDisplaySubtitle(entry: CalorieEntry): String {
    val source = sourceLabel(entry.source)
    return if (entry.name.isBlank()) {
        source
    } else {
        stringResource(
            if (entry.type == CalorieEntryType.INTAKE) {
                R.string.entry_subtitle_intake_template
            } else {
                R.string.entry_subtitle_burned_template
            },
            source
        )
    }
}

@Composable
internal fun deleteEntryLabel(entry: CalorieEntry): String {
    if (entry.name.isNotBlank()) {
        return entry.name
    }

    return if (entry.type == CalorieEntryType.INTAKE) {
        stringResource(R.string.entry_type_intake)
    } else {
        stringResource(R.string.entry_type_burned)
    }
}

internal fun averageEntryCalories(uiState: TrackerUiState): Int {
    return if (uiState.entries.isEmpty()) 0 else uiState.entries.sumOf { it.amount } / uiState.entries.size
}

internal fun historyDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
}

internal fun entryDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
}

internal fun weekDayFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
}

internal fun compactTrendDayFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
}

internal fun trendWindowFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
}

internal fun trendWindowLabel(startEpochDay: Long, endEpochDay: Long): String {
    val formatter = trendWindowFormatter()
    val start = LocalDate.ofEpochDay(startEpochDay).format(formatter)
    val end = LocalDate.ofEpochDay(endEpochDay).format(formatter)
    return "$start - $end"
}
