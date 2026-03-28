package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.DailyCalorieTrendPoint
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.Olive
import java.time.LocalDate
import kotlin.math.abs

@Composable
internal fun TrendSection(
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
