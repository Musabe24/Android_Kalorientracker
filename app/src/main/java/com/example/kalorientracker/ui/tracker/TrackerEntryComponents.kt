package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Olive

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
