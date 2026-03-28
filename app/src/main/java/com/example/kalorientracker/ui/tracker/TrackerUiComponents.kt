package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieInputValidationError
import com.example.kalorientracker.domain.calorie.GoalTargetValidationError
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.Panel
import com.example.kalorientracker.ui.theme.Sky
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun trackerCardColor(): Color = MaterialTheme.colorScheme.surface

@Composable
internal fun trackerPanelColor(alpha: Float = 1f): Color =
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

@Composable
internal fun trackerOutlineColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

@Composable
internal fun trackerPrimaryTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
internal fun trackerSecondaryTextColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)

@Composable
internal fun trackerEyebrowColor(): Color = MaterialTheme.colorScheme.primary

@Composable
internal fun SectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = trackerEyebrowColor()
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = trackerPrimaryTextColor()
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = trackerSecondaryTextColor()
        )
    }
}

@Composable
internal fun MetricPill(
    label: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}

internal data class SelectionOption(
    val label: String,
    val selected: Boolean,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
internal fun SelectionGroup(
    title: String,
    options: List<SelectionOption>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = trackerSecondaryTextColor()
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                SelectionChip(option = option)
            }
        }
    }
}

@Composable
internal fun SelectionChip(option: SelectionOption) {
    val backgroundColor = if (option.selected) option.accent.copy(alpha = 0.18f) else trackerCardColor()
    val borderColor = if (option.selected) option.accent else trackerOutlineColor()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = option.onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(option.accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
            color = trackerPrimaryTextColor()
        )
    }
}

@Composable
internal fun EntryRowCard(
    entry: CalorieEntry,
    index: Int,
    onEditEntryClicked: () -> Unit,
    onDeleteEntryClicked: () -> Unit
) {
    val accent = if (entry.type == CalorieEntryType.INTAKE) Olive else Coral

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = trackerPanelColor(alpha = 0.72f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Column {
                        Text(
                            text = entryDisplayTitle(entry, index),
                            style = MaterialTheme.typography.titleMedium,
                            color = trackerPrimaryTextColor()
                        )
                        Text(
                            text = entryDisplaySubtitle(entry),
                            style = MaterialTheme.typography.bodySmall,
                            color = trackerSecondaryTextColor()
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.entry_calories_template, entry.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = trackerPrimaryTextColor()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onEditEntryClicked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = trackerPrimaryTextColor())
                ) {
                    Text(text = stringResource(R.string.edit_entry_button))
                }
                TextButton(
                    onClick = onDeleteEntryClicked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = trackerPrimaryTextColor())
                ) {
                    Text(text = stringResource(R.string.delete_entry_button))
                }
            }
        }
    }
}

@Composable
internal fun EmptyEntriesState(
    title: String = stringResource(R.string.empty_entries_title),
    message: String = stringResource(R.string.empty_entries_message)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = trackerCardColor())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_entries_badge),
                    style = MaterialTheme.typography.titleMedium,
                    color = Gold,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = trackerPrimaryTextColor()
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = trackerSecondaryTextColor(),
                textAlign = TextAlign.Center
            )
        }
    }
}

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
