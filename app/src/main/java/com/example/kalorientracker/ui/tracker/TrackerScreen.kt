package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.CalorieHistoryDay
import com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.CoralDeep
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.InkMuted
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.OliveDeep
import com.example.kalorientracker.ui.theme.Panel
import com.example.kalorientracker.ui.theme.PanelStrong
import com.example.kalorientracker.ui.theme.Paper
import com.example.kalorientracker.ui.theme.Sky
import com.example.kalorientracker.ui.theme.WhiteSmoke
import com.example.kalorientracker.ui.theme.KalorientrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private const val DAILY_REFERENCE_TARGET = 2200

object TrackerScreenTestTags {
    const val CALORIE_INPUT_FIELD = "calorie_input_field"
    const val ADD_ENTRY_BUTTON = "add_entry_button"
}

@Composable
fun TrackerScreen(viewModel: TrackerViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackerContent(
        uiState = uiState,
        onCalorieInputChanged = viewModel::onCalorieInputChanged,
        onTypeSelected = viewModel::onEntryTypeSelected,
        onSourceSelected = viewModel::onEntrySourceSelected,
        onSaveEntryClicked = viewModel::saveEntry,
        onEditEntryClicked = viewModel::startEditing,
        onDeleteEntryClicked = viewModel::requestDeleteEntry,
        onCancelEditingClicked = viewModel::cancelEditing,
        onHistoryFilterSelected = viewModel::selectHistoryFilter,
        onDismissDeleteDialog = viewModel::dismissDeleteEntry,
        onConfirmDeleteEntry = viewModel::confirmDeleteEntry,
        modifier = modifier
    )
}

@Composable
fun TrackerContent(
    uiState: TrackerUiState,
    onCalorieInputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onSaveEntryClicked: () -> Unit,
    onEditEntryClicked: (CalorieEntry) -> Unit,
    onDeleteEntryClicked: (CalorieEntry) -> Unit,
    onCancelEditingClicked: () -> Unit,
    onHistoryFilterSelected: (HistoryFilter) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Panel.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        DecorativeBackdrop()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                DashboardHeader(uiState = uiState)
            }

            item {
                HeroSummaryCard(uiState = uiState)
            }

            item {
                QuickStatsRow(uiState = uiState)
            }

            item {
                WeeklyTrendSection(uiState = uiState)
            }

            item {
                EntryComposerCard(
                    uiState = uiState,
                    onCalorieInputChanged = onCalorieInputChanged,
                    onTypeSelected = onTypeSelected,
                    onSourceSelected = onSourceSelected,
                    onSaveEntryClicked = onSaveEntryClicked,
                    onCancelEditingClicked = onCancelEditingClicked
                )
            }

            item {
                SectionTitle(
                    eyebrow = stringResource(R.string.timeline_eyebrow),
                    title = stringResource(R.string.history_title),
                    subtitle = stringResource(R.string.timeline_subtitle)
                )
            }

            item {
                HistoryFilterRow(
                    selectedFilter = uiState.selectedHistoryFilter,
                    onHistoryFilterSelected = onHistoryFilterSelected
                )
            }

            if (uiState.hasHistory) {
                itemsIndexed(uiState.filteredHistoryDays) { index, day ->
                    HistoryDayCard(
                        index = index + 1,
                        historyDay = day,
                        onEditEntryClicked = onEditEntryClicked,
                        onDeleteEntryClicked = onDeleteEntryClicked
                    )
                }
            } else {
                item {
                    EmptyEntriesState()
                }
            }
        }

        uiState.pendingDeleteEntry?.let { entry ->
            DeleteEntryDialog(
                entry = entry,
                onDismiss = onDismissDeleteDialog,
                onConfirm = onConfirmDeleteEntry
            )
        }
    }
}

@Composable
private fun DecorativeBackdrop() {
    Box(
        modifier = Modifier
            .padding(start = 220.dp, top = 12.dp)
            .size(160.dp)
            .clip(CircleShape)
            .background(Gold.copy(alpha = 0.18f))
    )
    Box(
        modifier = Modifier
            .padding(start = 8.dp, top = 240.dp)
            .size(width = 120.dp, height = 220.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Sky.copy(alpha = 0.12f))
    )
}

@Composable
private fun DashboardHeader(uiState: TrackerUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.dashboard_eyebrow),
            style = MaterialTheme.typography.labelLarge,
            color = CoralDeep
        )
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle, uiState.entries.size),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeroSummaryCard(uiState: TrackerUiState) {
    val balanceAccent = if (uiState.netCalories >= 0) Olive else Coral
    val balanceLabel = if (uiState.netCalories >= 0) {
        stringResource(R.string.balance_remaining_label)
    } else {
        stringResource(R.string.balance_surplus_label)
    }
    val balanceValue = abs(DAILY_REFERENCE_TARGET - uiState.netCalories)

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Ink),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Ink, OliveDeep.copy(alpha = 0.95f), Ink)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.summary_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = Gold
                    )
                    Text(
                        text = stringResource(R.string.net_calories_template, uiState.netCalories),
                        style = MaterialTheme.typography.displaySmall,
                        color = WhiteSmoke
                    )
                }
                MetricPill(
                    label = stringResource(R.string.day_title_template, uiState.dayNumber),
                    backgroundColor = WhiteSmoke.copy(alpha = 0.1f),
                    contentColor = WhiteSmoke
                )
            }

            Text(
                text = balanceLabel,
                style = MaterialTheme.typography.titleMedium,
                color = WhiteSmoke.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.reference_target_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteSmoke.copy(alpha = 0.7f)
                    )
                    Text(
                        text = stringResource(R.string.entry_calories_template, balanceValue),
                        style = MaterialTheme.typography.titleLarge,
                        color = balanceAccent
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(balanceAccent.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.entry_count_template, uiState.entries.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteSmoke
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(uiState: TrackerUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.total_intake_short),
            value = stringResource(R.string.entry_calories_template, uiState.totalIntake),
            accent = Olive
        )
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.total_burned_short),
            value = stringResource(R.string.entry_calories_template, uiState.totalBurned),
            accent = Coral
        )
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.avg_entry_short),
            value = stringResource(
                R.string.entry_calories_template,
                averageEntryCalories(uiState)
            ),
            accent = Sky
        )
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Paper)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WeeklyTrendSection(uiState: TrackerUiState) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionTitle(
                eyebrow = stringResource(R.string.weekly_trend_eyebrow),
                title = stringResource(R.string.weekly_trend_title),
                subtitle = stringResource(R.string.weekly_trend_subtitle)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 164.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxNet = uiState.weeklyTrend.maxOfOrNull { abs(it.netCalories) }?.coerceAtLeast(1) ?: 1
                uiState.weeklyTrend.forEach { point ->
                    WeeklyTrendBar(
                        modifier = Modifier.weight(1f),
                        point = point,
                        maxNet = maxNet
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterRow(
    selectedFilter: HistoryFilter,
    onHistoryFilterSelected: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            HistoryFilter.Today to stringResource(R.string.history_filter_today),
            HistoryFilter.SevenDays to stringResource(R.string.history_filter_seven_days),
            HistoryFilter.AllTime to stringResource(R.string.history_filter_all_time)
        ).forEach { (filter, label) ->
            SelectionChip(
                option = SelectionOption(
                    label = label,
                    selected = selectedFilter == filter,
                    accent = Ink,
                    onClick = { onHistoryFilterSelected(filter) }
                )
            )
        }
    }
}

@Composable
private fun WeeklyTrendBar(
    modifier: Modifier = Modifier,
    point: DailyCalorieTrendPoint,
    maxNet: Int
) {
    val net = point.netCalories
    val accent = if (net >= 0) Olive else Coral
    val fillFraction = (abs(net).toFloat() / maxNet.toFloat()).coerceIn(0.14f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.compact_calorie_template, net),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Panel.copy(alpha = 0.75f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(fillFraction)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(accent.copy(alpha = 0.4f), accent)
                        )
                    )
            )
        }
        Text(
            text = LocalDate.ofEpochDay(point.epochDay).format(weekDayFormatter()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EntryComposerCard(
    uiState: TrackerUiState,
    onCalorieInputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onSaveEntryClicked: () -> Unit,
    onCancelEditingClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PanelStrong.copy(alpha = 0.58f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                eyebrow = stringResource(R.string.composer_eyebrow),
                title = stringResource(
                    if (uiState.isEditing) {
                        R.string.edit_entry_form_title
                    } else {
                        R.string.entry_form_title
                    }
                ),
                subtitle = stringResource(R.string.composer_subtitle)
            )

            OutlinedTextField(
                value = uiState.calorieInput,
                onValueChange = onCalorieInputChanged,
                label = { Text(stringResource(R.string.calorie_input_label)) },
                prefix = {
                    Text(
                        text = "kcal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.inputError != null,
                supportingText = {
                    val supportingMessage = uiState.inputError ?: stringResource(R.string.calorie_input_hint)
                    Text(supportingMessage)
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TrackerScreenTestTags.CALORIE_INPUT_FIELD)
            )

            SelectionGroup(
                title = stringResource(R.string.entry_type_title),
                options = listOf(
                    SelectionOption(
                        label = stringResource(R.string.entry_type_intake),
                        selected = uiState.selectedType == CalorieEntryType.INTAKE,
                        accent = Olive,
                        onClick = { onTypeSelected(CalorieEntryType.INTAKE) }
                    ),
                    SelectionOption(
                        label = stringResource(R.string.entry_type_burned),
                        selected = uiState.selectedType == CalorieEntryType.BURNED,
                        accent = Coral,
                        onClick = { onTypeSelected(CalorieEntryType.BURNED) }
                    )
                )
            )

            SelectionGroup(
                title = stringResource(R.string.entry_source_title),
                options = listOf(
                    SelectionOption(
                        label = stringResource(R.string.entry_source_meal),
                        selected = uiState.selectedSource == CalorieEntrySource.MEAL,
                        accent = Gold,
                        onClick = { onSourceSelected(CalorieEntrySource.MEAL) }
                    ),
                    SelectionOption(
                        label = stringResource(R.string.entry_source_watch),
                        selected = uiState.selectedSource == CalorieEntrySource.WATCH,
                        accent = Sky,
                        onClick = { onSourceSelected(CalorieEntrySource.WATCH) }
                    ),
                    SelectionOption(
                        label = stringResource(R.string.entry_source_manual),
                        selected = uiState.selectedSource == CalorieEntrySource.MANUAL,
                        accent = InkMuted,
                        onClick = { onSourceSelected(CalorieEntrySource.MANUAL) }
                    )
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveEntryClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink,
                        contentColor = WhiteSmoke
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(TrackerScreenTestTags.ADD_ENTRY_BUTTON)
                ) {
                    Text(
                        text = stringResource(
                            if (uiState.isEditing) {
                                R.string.update_entry_button
                            } else {
                                R.string.add_entry_button
                            }
                        )
                    )
                }

                if (uiState.isEditing) {
                    TextButton(
                        onClick = onCancelEditingClicked,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_edit_button),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionGroup(
    title: String,
    options: List<SelectionOption>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun SelectionChip(option: SelectionOption) {
    val backgroundColor = if (option.selected) option.accent.copy(alpha = 0.18f) else Paper
    val borderColor = if (option.selected) option.accent else PanelStrong
    val textColor = if (option.selected) Ink else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
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
            color = textColor
        )
    }
}

@Composable
private fun SectionTitle(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = CoralDeep
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryDayCard(
    index: Int,
    historyDay: CalorieHistoryDay,
    onEditEntryClicked: (CalorieEntry) -> Unit,
    onDeleteEntryClicked: (CalorieEntry) -> Unit
) {
    val accent = if (historyDay.netCalories >= 0) Olive else Coral

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Paper)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 6.dp, height = 56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(accent)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.history_day_index_template, index),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = LocalDate.ofEpochDay(historyDay.epochDay).format(historyDateFormatter()),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.history_day_summary_template,
                                historyDay.entries.size,
                                historyDay.totalIntake,
                                historyDay.totalBurned
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    MetricPill(
                        label = stringResource(R.string.net_short_label),
                        backgroundColor = accent.copy(alpha = 0.14f),
                        contentColor = accent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.entry_calories_template, historyDay.netCalories),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                historyDay.entries.forEachIndexed { entryIndex, entry ->
                    HistoryEntryRow(
                        entry = entry,
                        index = entryIndex + 1,
                        onEditEntryClicked = { onEditEntryClicked(entry) },
                        onDeleteEntryClicked = { onDeleteEntryClicked(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(
    entry: CalorieEntry,
    index: Int,
    onEditEntryClicked: () -> Unit,
    onDeleteEntryClicked: () -> Unit
) {
    val accent = if (entry.type == CalorieEntryType.INTAKE) Olive else Coral

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.45f)),
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
                            text = stringResource(
                                if (entry.type == CalorieEntryType.INTAKE) {
                                    R.string.history_intake_entry_template
                                } else {
                                    R.string.history_burned_entry_template
                                },
                                index
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sourceLabel(entry.source),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.entry_calories_template, entry.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onEditEntryClicked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(R.string.edit_entry_button))
                }
                TextButton(
                    onClick = onDeleteEntryClicked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(R.string.delete_entry_button))
                }
            }
        }
    }
}

@Composable
private fun DeleteEntryDialog(
    entry: CalorieEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_dialog_message,
                    if (entry.type == CalorieEntryType.INTAKE) {
                        stringResource(R.string.entry_type_intake)
                    } else {
                        stringResource(R.string.entry_type_burned)
                    },
                    entry.amount
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Coral)
            ) {
                Text(text = stringResource(R.string.delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.delete_dialog_cancel))
            }
        }
    )
}

private fun HistorySectionPreviewDay(): List<CalorieHistoryDay> {
    return listOf(
        CalorieHistoryDay(
            epochDay = LocalDate.of(2026, 3, 28).toEpochDay(),
            entries = listOf(
                CalorieEntry(
                    id = "meal-1",
                    amount = 650,
                    type = CalorieEntryType.INTAKE,
                    source = CalorieEntrySource.MEAL,
                    recordedOnEpochDay = LocalDate.of(2026, 3, 28).toEpochDay()
                ),
                CalorieEntry(
                    id = "burned-1",
                    amount = 420,
                    type = CalorieEntryType.BURNED,
                    source = CalorieEntrySource.WATCH,
                    recordedOnEpochDay = LocalDate.of(2026, 3, 28).toEpochDay()
                )
            ),
            totalIntake = 650,
            totalBurned = 420,
            netCalories = 230
        )
    )
}

@Composable
private fun MetricPill(
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

@Composable
private fun EmptyEntriesState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Paper)
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
                text = stringResource(R.string.empty_entries_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.empty_entries_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerContentPreview() {
    KalorientrackerTheme {
        TrackerContent(
            uiState = TrackerUiState(
                entries = listOf(
                    CalorieEntry(
                        id = "meal-1",
                        amount = 650,
                        type = CalorieEntryType.INTAKE,
                        source = CalorieEntrySource.MEAL,
                        recordedOnEpochDay = LocalDate.of(2026, 3, 28).toEpochDay()
                    ),
                    CalorieEntry(
                        id = "burned-1",
                        amount = 420,
                        type = CalorieEntryType.BURNED,
                        source = CalorieEntrySource.WATCH,
                        recordedOnEpochDay = LocalDate.of(2026, 3, 28).toEpochDay()
                    )
                ),
                historyDays = HistorySectionPreviewDay(),
                weeklyTrend = listOf(
                    DailyCalorieTrendPoint(20534L, 1800, 200, 1600),
                    DailyCalorieTrendPoint(20535L, 2100, 250, 1850),
                    DailyCalorieTrendPoint(20536L, 1950, 300, 1650),
                    DailyCalorieTrendPoint(20537L, 2300, 350, 1950),
                    DailyCalorieTrendPoint(20538L, 1600, 180, 1420),
                    DailyCalorieTrendPoint(20539L, 2450, 400, 2050),
                    DailyCalorieTrendPoint(20540L, 650, 420, 230)
                ),
                totalIntake = 650,
                totalBurned = 420,
                netCalories = 230
            ),
            onCalorieInputChanged = {},
            onTypeSelected = {},
            onSourceSelected = {},
            onSaveEntryClicked = {},
            onEditEntryClicked = {},
            onDeleteEntryClicked = {},
            onCancelEditingClicked = {},
            onHistoryFilterSelected = {},
            onDismissDeleteDialog = {},
            onConfirmDeleteEntry = {}
        )
    }
}

private data class SelectionOption(
    val label: String,
    val selected: Boolean,
    val accent: Color,
    val onClick: () -> Unit
)

private fun averageEntryCalories(uiState: TrackerUiState): Int {
    return if (uiState.entries.isEmpty()) 0 else uiState.entries.sumOf { it.amount } / uiState.entries.size
}

@Composable
private fun sourceLabel(source: CalorieEntrySource): String {
    return when (source) {
        CalorieEntrySource.MEAL -> stringResource(R.string.entry_source_meal)
        CalorieEntrySource.WATCH -> stringResource(R.string.entry_source_watch)
        CalorieEntrySource.MANUAL -> stringResource(R.string.entry_source_manual)
    }
}

private fun weekDayFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
}

private fun historyDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
}
