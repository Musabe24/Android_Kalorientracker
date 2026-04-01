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
    onAiApiKeyInputChanged: (String) -> Unit,
    onStartEditingAiSettings: () -> Unit,
    onCancelAiSettingsEditing: () -> Unit,
    onSaveAiApiKey: () -> Unit,
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
        item {
            AiSettingsSection(
                uiState = uiState,
                onApiKeyInputChanged = onAiApiKeyInputChanged,
                onStartEditing = onStartEditingAiSettings,
                onCancelEditing = onCancelAiSettingsEditing,
                onSaveApiKey = onSaveAiApiKey
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
private fun AiSettingsSection(
    uiState: TrackerUiState,
    onApiKeyInputChanged: (String) -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveApiKey: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = trackerCardColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                eyebrow = "AI SERVICES",
                title = "Gemini API Key",
                subtitle = "Required for Magic Log and AI Insights."
            )

            if (uiState.isEditingAiSettings) {
                androidx.compose.material3.OutlinedTextField(
                    value = uiState.aiApiKeyInput,
                    onValueChange = onApiKeyInputChanged,
                    label = { Text("API Key") },
                    placeholder = { Text("Paste your key here") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    androidx.compose.material3.TextButton(onClick = onCancelEditing) {
                        Text("Cancel")
                    }
                    androidx.compose.material3.Button(
                        onClick = onSaveApiKey,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Key")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.aiApiKey.isNullOrBlank()) "No key configured" else "••••••••••••••••",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.aiApiKey.isNullOrBlank()) Coral else trackerPrimaryTextColor()
                    )
                    androidx.compose.material3.TextButton(onClick = onStartEditing) {
                        Text(if (uiState.aiApiKey.isNullOrBlank()) "Add Key" else "Change")
                    }
                }
            }
        }
    }
}
