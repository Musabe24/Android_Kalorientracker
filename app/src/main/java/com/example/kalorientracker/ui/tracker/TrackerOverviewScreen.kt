package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint
import com.example.kalorientracker.domain.calorie.GoalProgressInsights
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.CoralDeep
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.OliveDeep
import com.example.kalorientracker.ui.theme.Panel
import com.example.kalorientracker.ui.theme.Sky
import com.example.kalorientracker.ui.theme.WhiteSmoke
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun TrackerOverviewScreen(
    uiState: TrackerUiState,
    onTrendRangeSelected: (TrendRange) -> Unit,
    onEarlierTrendWindowSelected: () -> Unit,
    onLaterTrendWindowSelected: () -> Unit,
    onStartEditingGoalTarget: () -> Unit,
    onGoalTargetInputChanged: (String) -> Unit,
    onCancelGoalTargetEditing: () -> Unit,
    onSaveGoalTarget: () -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = stringResource(R.string.navigation_overview).uppercase(),
                title = stringResource(R.string.overview_title),
                subtitle = stringResource(R.string.overview_subtitle)
            )
        }
        item { HeroSummaryCard(uiState = uiState) }
        item { QuickStatsRow(uiState = uiState) }
        uiState.goalProgressInsights?.let { insights ->
            item {
                GoalProgressSection(
                    goalProgressInsights = insights,
                    uiState = uiState,
                    onStartEditingGoalTarget = onStartEditingGoalTarget,
                    onGoalTargetInputChanged = onGoalTargetInputChanged,
                    onCancelGoalTargetEditing = onCancelGoalTargetEditing,
                    onSaveGoalTarget = onSaveGoalTarget
                )
            }
        }
        item {
            TrendSection(
                uiState = uiState,
                onTrendRangeSelected = onTrendRangeSelected,
                onEarlierTrendWindowSelected = onEarlierTrendWindowSelected,
                onLaterTrendWindowSelected = onLaterTrendWindowSelected
            )
        }
    }
}

@Composable
private fun HeroSummaryCard(uiState: TrackerUiState) {
    val balanceAccent = if (uiState.netCalories >= 0) Olive else Coral
    val targetCalories = uiState.goalProgressInsights?.targetCalories ?: uiState.targetCalories
    val balanceLabel = if (uiState.netCalories >= 0) {
        stringResource(R.string.balance_remaining_label)
    } else {
        stringResource(R.string.balance_surplus_label)
    }

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
                    backgroundColor = WhiteSmoke.copy(alpha = 0.14f),
                    contentColor = WhiteSmoke
                )
            }

            Text(
                text = balanceLabel,
                style = MaterialTheme.typography.titleMedium,
                color = WhiteSmoke.copy(alpha = 0.92f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.reference_target_label, targetCalories),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteSmoke.copy(alpha = 0.82f)
                    )
                    Text(
                        text = stringResource(
                            R.string.entry_calories_template,
                            abs(targetCalories - uiState.netCalories)
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = WhiteSmoke
                    )
                }
                MetricPill(
                    label = stringResource(R.string.entry_count_template, uiState.entries.size),
                    backgroundColor = balanceAccent.copy(alpha = 0.18f),
                    contentColor = WhiteSmoke
                )
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
        OverviewStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.total_intake_short),
            value = stringResource(R.string.entry_calories_template, uiState.totalIntake),
            accent = Olive
        )
        OverviewStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.total_burned_short),
            value = stringResource(R.string.entry_calories_template, uiState.totalBurned),
            accent = Coral
        )
        OverviewStatCard(
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
private fun OverviewStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = trackerCardColor())
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
                color = trackerSecondaryTextColor()
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = trackerPrimaryTextColor()
            )
        }
    }
}

@Composable
private fun GoalProgressSection(
    goalProgressInsights: GoalProgressInsights,
    uiState: TrackerUiState,
    onStartEditingGoalTarget: () -> Unit,
    onGoalTargetInputChanged: (String) -> Unit,
    onCancelGoalTargetEditing: () -> Unit,
    onSaveGoalTarget: () -> Unit
) {
    val accent = if (goalProgressInsights.remainingCalories >= 0) Olive else Coral

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = trackerCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionHeader(
                eyebrow = stringResource(R.string.goal_eyebrow),
                title = stringResource(R.string.goal_title),
                subtitle = stringResource(R.string.goal_subtitle, goalProgressInsights.targetCalories)
            )

            if (uiState.isEditingGoalTarget) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = uiState.targetCaloriesInput,
                        onValueChange = onGoalTargetInputChanged,
                        label = { Text(stringResource(R.string.goal_target_input_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text(
                                goalTargetErrorMessage(uiState.goalTargetError)
                                    ?: stringResource(R.string.goal_target_input_hint)
                            )
                        },
                        isError = uiState.goalTargetError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onSaveGoalTarget, shape = RoundedCornerShape(16.dp)) {
                        Text(text = stringResource(R.string.goal_target_save_button))
                    }
                    TextButton(onClick = onCancelGoalTargetEditing) {
                        Text(text = stringResource(R.string.goal_target_cancel_button))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.goal_target_value, goalProgressInsights.targetCalories),
                        style = MaterialTheme.typography.titleMedium,
                        color = trackerPrimaryTextColor()
                    )
                    TextButton(onClick = onStartEditingGoalTarget) {
                        Text(text = stringResource(R.string.goal_target_edit_button))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Panel.copy(alpha = 0.8f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goalProgressInsights.progressRatio.coerceAtLeast(0.04f))
                        .height(18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(accent.copy(alpha = 0.75f), accent)
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoalMetric(
                    label = stringResource(R.string.goal_remaining_label),
                    value = stringResource(R.string.entry_calories_template, abs(goalProgressInsights.remainingCalories)),
                    accent = accent
                )
                GoalMetric(
                    label = stringResource(R.string.goal_average_label),
                    value = stringResource(R.string.entry_calories_template, goalProgressInsights.averageNetCalories),
                    accent = Sky
                )
                GoalMetric(
                    label = stringResource(R.string.goal_hit_days_label),
                    value = stringResource(R.string.goal_hit_days_value, goalProgressInsights.targetHitDays),
                    accent = Gold
                )
                GoalMetric(
                    label = stringResource(R.string.goal_streak_label),
                    value = stringResource(R.string.goal_streak_value, goalProgressInsights.consistencyStreak),
                    accent = CoralDeep
                )
            }
        }
    }
}

@Composable
private fun GoalMetric(
    label: String,
    value: String,
    accent: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = trackerPrimaryTextColor())
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = trackerSecondaryTextColor(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TrendSection(
    uiState: TrackerUiState,
    onTrendRangeSelected: (TrendRange) -> Unit,
    onEarlierTrendWindowSelected: () -> Unit,
    onLaterTrendWindowSelected: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = trackerCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionHeader(
                eyebrow = stringResource(R.string.weekly_trend_eyebrow),
                title = stringResource(R.string.weekly_trend_title),
                subtitle = stringResource(R.string.weekly_trend_subtitle)
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    TrendRange.SevenDays to stringResource(R.string.trend_range_seven_days),
                    TrendRange.ThirtyDays to stringResource(R.string.trend_range_thirty_days),
                    TrendRange.AllTime to stringResource(R.string.trend_range_all_time)
                ).forEach { (range, label) ->
                    SelectionChip(
                        option = SelectionOption(
                            label = label,
                            selected = uiState.selectedTrendRange == range,
                            accent = Ink,
                            onClick = { onTrendRangeSelected(range) }
                        )
                    )
                }
            }

            if (uiState.selectedTrendRange != TrendRange.AllTime && uiState.visibleTrendPoints.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEarlierTrendWindowSelected,
                        enabled = uiState.canNavigateToEarlierTrendWindow
                    ) {
                        Text(text = stringResource(R.string.trend_navigation_earlier))
                    }
                    Text(
                        text = trendWindowLabel(
                            uiState.visibleTrendPoints.first().epochDay,
                            uiState.visibleTrendPoints.last().epochDay
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = trackerPrimaryTextColor()
                    )
                    TextButton(
                        onClick = onLaterTrendWindowSelected,
                        enabled = uiState.canNavigateToLaterTrendWindow
                    ) {
                        Text(text = stringResource(R.string.trend_navigation_later))
                    }
                }
            }

            if (uiState.visibleTrendPoints.isEmpty()) {
                EmptyEntriesState(
                    title = stringResource(R.string.trend_empty_title),
                    message = stringResource(R.string.trend_empty_message)
                )
            } else {
                val visibleTrendPoints = uiState.visibleTrendPoints
                val maxNet = visibleTrendPoints.maxOfOrNull { abs(it.netCalories) }?.coerceAtLeast(1) ?: 1

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .heightIn(min = 164.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    visibleTrendPoints.forEach { point ->
                        TrendBar(
                            point = point,
                            maxNet = maxNet,
                            isCondensed = visibleTrendPoints.size > 14
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBar(
    point: DailyCalorieTrendPoint,
    maxNet: Int,
    isCondensed: Boolean
) {
    val accent = if (point.netCalories >= 0) Olive else Coral
    val fillFraction = (abs(point.netCalories).toFloat() / maxNet.toFloat()).coerceIn(0.14f, 1f)
    val barWidth = if (isCondensed) 24.dp else 38.dp

    Column(
        modifier = Modifier.width(barWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.compact_calorie_template, point.netCalories),
            style = MaterialTheme.typography.bodySmall,
            color = trackerSecondaryTextColor()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(trackerPanelColor(alpha = 0.85f)),
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
            text = LocalDate.ofEpochDay(point.epochDay).format(
                if (isCondensed) compactTrendDayFormatter() else weekDayFormatter()
            ),
            style = MaterialTheme.typography.bodySmall,
            color = trackerPrimaryTextColor(),
            fontWeight = FontWeight.SemiBold
        )
    }
}
