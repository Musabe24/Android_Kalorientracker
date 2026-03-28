package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.ui.theme.KalorientrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        onDeleteEntryClicked = viewModel::deleteEntry,
        onCancelEditingClicked = viewModel::cancelEditing,
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
    onDeleteEntryClicked: (String) -> Unit,
    onCancelEditingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummarySection(uiState = uiState)
        }

        item {
            EntryFormSection(
                uiState = uiState,
                onCalorieInputChanged = onCalorieInputChanged,
                onTypeSelected = onTypeSelected,
                onSourceSelected = onSourceSelected,
                onSaveEntryClicked = onSaveEntryClicked,
                onCancelEditingClicked = onCancelEditingClicked
            )
        }

        item {
            Text(
                text = stringResource(R.string.entry_list_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (uiState.hasEntries) {
            itemsIndexed(uiState.entries.asReversed()) { index, entry ->
                EntryItem(
                    index = index + 1,
                    entry = entry,
                    onEditEntryClicked = { onEditEntryClicked(entry) },
                    onDeleteEntryClicked = { onDeleteEntryClicked(entry.id) }
                )
            }
        } else {
            item {
                EmptyEntriesState()
            }
        }
    }
}

@Composable
private fun SummarySection(uiState: TrackerUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.day_title_template, uiState.dayNumber),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.total_intake_template, uiState.totalIntake))
            Text(stringResource(R.string.total_burned_template, uiState.totalBurned))
            Text(
                text = stringResource(R.string.net_calories_template, uiState.netCalories),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EntryFormSection(
    uiState: TrackerUiState,
    onCalorieInputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onSaveEntryClicked: () -> Unit,
    onCancelEditingClicked: () -> Unit
) {
    Column {
        Text(
            text = stringResource(
                if (uiState.isEditing) {
                    R.string.edit_entry_form_title
                } else {
                    R.string.entry_form_title
                }
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.calorieInput,
            onValueChange = onCalorieInputChanged,
            label = { Text(stringResource(R.string.calorie_input_label)) },
            isError = uiState.inputError != null,
            supportingText = {
                uiState.inputError?.let { error ->
                    Text(error)
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TrackerScreenTestTags.CALORIE_INPUT_FIELD)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TypePicker(
            selectedType = uiState.selectedType,
            onTypeSelected = onTypeSelected
        )
        SourcePicker(
            selectedSource = uiState.selectedSource,
            onSourceSelected = onSourceSelected
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSaveEntryClicked,
                modifier = Modifier.testTag(TrackerScreenTestTags.ADD_ENTRY_BUTTON)
            ) {
                Text(
                    stringResource(
                        if (uiState.isEditing) {
                            R.string.update_entry_button
                        } else {
                            R.string.add_entry_button
                        }
                    )
                )
            }

            if (uiState.isEditing) {
                TextButton(onClick = onCancelEditingClicked) {
                    Text(stringResource(R.string.cancel_edit_button))
                }
            }
        }
    }
}

@Composable
private fun EmptyEntriesState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.empty_entries_message),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TypePicker(
    selectedType: CalorieEntryType,
    onTypeSelected: (CalorieEntryType) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.entry_type_title),
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row {
                RadioButton(
                    selected = selectedType == CalorieEntryType.INTAKE,
                    onClick = { onTypeSelected(CalorieEntryType.INTAKE) }
                )
                Text(stringResource(R.string.entry_type_intake), modifier = Modifier.padding(top = 12.dp))
            }
            Row {
                RadioButton(
                    selected = selectedType == CalorieEntryType.BURNED,
                    onClick = { onTypeSelected(CalorieEntryType.BURNED) }
                )
                Text(stringResource(R.string.entry_type_burned), modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun SourcePicker(
    selectedSource: CalorieEntrySource,
    onSourceSelected: (CalorieEntrySource) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.entry_source_title),
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SourceOption(
                selected = selectedSource == CalorieEntrySource.MEAL,
                label = stringResource(R.string.entry_source_meal),
                onClick = { onSourceSelected(CalorieEntrySource.MEAL) }
            )
            SourceOption(
                selected = selectedSource == CalorieEntrySource.WATCH,
                label = stringResource(R.string.entry_source_watch),
                onClick = { onSourceSelected(CalorieEntrySource.WATCH) }
            )
            SourceOption(
                selected = selectedSource == CalorieEntrySource.MANUAL,
                label = stringResource(R.string.entry_source_manual),
                onClick = { onSourceSelected(CalorieEntrySource.MANUAL) }
            )
        }
    }
}

@Composable
private fun SourceOption(selected: Boolean, label: String, onClick: () -> Unit) {
    Row {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun EntryItem(
    index: Int,
    entry: CalorieEntry,
    onEditEntryClicked: () -> Unit,
    onDeleteEntryClicked: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = stringResource(R.string.entry_index_template, index))
                    Text(
                        text = if (entry.type == CalorieEntryType.INTAKE) {
                            stringResource(R.string.entry_type_intake)
                        } else {
                            stringResource(R.string.entry_type_burned)
                        },
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(
                            R.string.entry_date_template,
                            LocalDate.ofEpochDay(entry.recordedOnEpochDay)
                                .format(entryDateFormatter())
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column {
                    Text(text = stringResource(R.string.entry_calories_template, entry.amount))
                    Text(
                        text = when (entry.source) {
                            CalorieEntrySource.MEAL -> stringResource(R.string.entry_source_meal)
                            CalorieEntrySource.WATCH -> stringResource(R.string.entry_source_watch)
                            CalorieEntrySource.MANUAL -> stringResource(R.string.entry_source_manual)
                        }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditEntryClicked) {
                    Text(stringResource(R.string.edit_entry_button))
                }
                TextButton(onClick = onDeleteEntryClicked) {
                    Text(stringResource(R.string.delete_entry_button))
                }
            }
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
            onCancelEditingClicked = {}
        )
    }
}

private fun entryDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.getDefault())
}
