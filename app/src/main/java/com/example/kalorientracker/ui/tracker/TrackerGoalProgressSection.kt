package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.GoalProgressInsights
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.CoralDeep
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.Panel
import com.example.kalorientracker.ui.theme.Sky
import kotlin.math.abs

@Composable
internal fun GoalProgressSection(
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
