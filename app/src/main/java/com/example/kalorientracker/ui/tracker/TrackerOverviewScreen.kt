package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.OliveDeep
import com.example.kalorientracker.ui.theme.Sky
import com.example.kalorientracker.ui.theme.WhiteSmoke
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
