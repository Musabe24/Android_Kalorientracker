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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieHistoryDay
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Olive
import java.time.LocalDate

@Composable
fun TrackerHistoryScreen(
    uiState: TrackerUiState,
    onHistoryFilterSelected: (HistoryFilter) -> Unit,
    onEditEntryClicked: (CalorieEntry) -> Unit,
    onDeleteEntryClicked: (CalorieEntry) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = stringResource(R.string.navigation_history).uppercase(),
                title = stringResource(R.string.history_title),
                subtitle = stringResource(R.string.history_screen_subtitle)
            )
        }
        item {
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
                            selected = uiState.selectedHistoryFilter == filter,
                            accent = com.example.kalorientracker.ui.theme.Ink,
                            onClick = { onHistoryFilterSelected(filter) }
                        )
                    )
                }
            }
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
            item { EmptyEntriesState() }
        }
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
        colors = CardDefaults.cardColors(containerColor = trackerCardColor())
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
                            color = trackerSecondaryTextColor()
                        )
                        Text(
                            text = LocalDate.ofEpochDay(historyDay.epochDay).format(historyDateFormatter()),
                            style = MaterialTheme.typography.titleLarge,
                            color = trackerPrimaryTextColor()
                        )
                        Text(
                            text = stringResource(
                                R.string.history_day_summary_template,
                                historyDay.entries.size,
                                historyDay.totalIntake,
                                historyDay.totalBurned
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = trackerSecondaryTextColor()
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    MetricPill(
                        label = stringResource(R.string.net_short_label),
                        backgroundColor = accent.copy(alpha = 0.14f),
                        contentColor = trackerPrimaryTextColor()
                    )
                    Text(
                        text = stringResource(R.string.entry_calories_template, historyDay.netCalories),
                        style = MaterialTheme.typography.titleLarge,
                        color = trackerPrimaryTextColor()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                historyDay.entries.forEachIndexed { entryIndex, entry ->
                    EntryRowCard(
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
