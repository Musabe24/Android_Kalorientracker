package com.example.kalorientracker.ui.home

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

object GreetingScreenTestTags {
    const val CALORIE_INPUT_FIELD = "calorie_input_field"
    const val ADD_ENTRY_BUTTON = "add_entry_button"
}

@Composable
fun GreetingScreen(viewModel: GreetingViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GreetingContent(
        uiState = uiState,
        onCalorieInputChanged = viewModel::onCalorieInputChanged,
        onTypeSelected = viewModel::onEntryTypeSelected,
        onSourceSelected = viewModel::onEntrySourceSelected,
        onAddEntryClicked = viewModel::addEntry,
        modifier = modifier
    )
}

@Composable
fun GreetingContent(
    uiState: GreetingUiState,
    onCalorieInputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onAddEntryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummarySection(uiState)
        }

        item {
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
                    .testTag(GreetingScreenTestTags.CALORIE_INPUT_FIELD)
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
            Button(
                onClick = onAddEntryClicked,
                modifier = Modifier.testTag(GreetingScreenTestTags.ADD_ENTRY_BUTTON)
            ) {
                Text(stringResource(R.string.add_entry_button))
            }
        }

        item {
            Text(
                text = stringResource(R.string.entry_list_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        itemsIndexed(uiState.entries.reversed()) { index, entry ->
            EntryItem(index = index + 1, entry = entry)
        }
    }
}

@Composable
private fun SummarySection(uiState: GreetingUiState) {
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
private fun TypePicker(
    selectedType: CalorieEntryType,
    onTypeSelected: (CalorieEntryType) -> Unit
) {
    Column {
        Text(text = stringResource(R.string.entry_type_title), style = MaterialTheme.typography.labelLarge)
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
        Text(text = stringResource(R.string.entry_source_title), style = MaterialTheme.typography.labelLarge)
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
private fun EntryItem(index: Int, entry: CalorieEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingContentPreview() {
    KalorientrackerTheme {
        GreetingContent(
            uiState = GreetingUiState(
                entries = listOf(
                    CalorieEntry(650, CalorieEntryType.INTAKE, CalorieEntrySource.MEAL),
                    CalorieEntry(420, CalorieEntryType.BURNED, CalorieEntrySource.WATCH)
                ),
                totalIntake = 650,
                totalBurned = 420,
                netCalories = 230
            ),
            onCalorieInputChanged = {},
            onTypeSelected = {},
            onSourceSelected = {},
            onAddEntryClicked = {}
        )
    }
}
