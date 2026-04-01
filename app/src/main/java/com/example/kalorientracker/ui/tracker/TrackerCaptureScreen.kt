package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.SupportedAiModel
import com.example.kalorientracker.ui.theme.Gold
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.InkMuted
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.Sky
import com.example.kalorientracker.ui.theme.WhiteSmoke
import java.time.LocalDate

@Composable
fun TrackerCaptureScreen(
    uiState: TrackerUiState,
    onEntryNameChanged: (String) -> Unit,
    onEntryInputModeSelected: (EntryInputMode) -> Unit,
    onCalorieInputChanged: (String) -> Unit,
    onConsumedAmountInputChanged: (String) -> Unit,
    onCaloriesPer100InputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onShowPreviousEntryDate: () -> Unit,
    onShowNextEntryDate: () -> Unit,
    onResetEntryDateToToday: () -> Unit,
    onSaveEntryClicked: () -> Unit,
    onCancelEditingClicked: () -> Unit,
    onEditEntryClicked: (CalorieEntry) -> Unit,
    onDeleteEntryClicked: (CalorieEntry) -> Unit,
    onShowDatePicker: () -> Unit,
    onAiMealDescriptionChanged: (String) -> Unit,
    onAnalyzeMealWithAi: () -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = stringResource(R.string.navigation_capture).uppercase(),
                title = stringResource(
                    if (uiState.isEditing) R.string.edit_entry_form_title else R.string.entry_form_title
                ),
                subtitle = stringResource(R.string.capture_screen_subtitle)
            )
        }
        item {
            EntryComposerCard(
                uiState = uiState,
                onEntryNameChanged = onEntryNameChanged,
                onEntryInputModeSelected = onEntryInputModeSelected,
                onCalorieInputChanged = onCalorieInputChanged,
                onConsumedAmountInputChanged = onConsumedAmountInputChanged,
                onCaloriesPer100InputChanged = onCaloriesPer100InputChanged,
                onTypeSelected = onTypeSelected,
                onSourceSelected = onSourceSelected,
                onShowPreviousEntryDate = onShowPreviousEntryDate,
                onShowNextEntryDate = onShowNextEntryDate,
                onResetEntryDateToToday = onResetEntryDateToToday,
                onSaveEntryClicked = onSaveEntryClicked,
                onCancelEditingClicked = onCancelEditingClicked,
                onShowDatePicker = onShowDatePicker,
                onAiMealDescriptionChanged = onAiMealDescriptionChanged,
                onAnalyzeMealWithAi = onAnalyzeMealWithAi
            )
        }
        item {
            SectionHeader(
                eyebrow = stringResource(R.string.summary_title).uppercase(),
                title = stringResource(R.string.capture_today_title),
                subtitle = stringResource(R.string.capture_today_subtitle)
            )
        }
        if (uiState.hasEntries) {
            itemsIndexed(uiState.entries) { index, entry ->
                EntryRowCard(
                    entry = entry,
                    index = index + 1,
                    onEditEntryClicked = { onEditEntryClicked(entry) },
                    onDeleteEntryClicked = { onDeleteEntryClicked(entry) }
                )
            }
        } else {
            item {
                EmptyEntriesState(
                    title = stringResource(R.string.capture_empty_title),
                    message = stringResource(R.string.capture_empty_message)
                )
            }
        }
    }
}

@Composable
private fun EntryComposerCard(
    uiState: TrackerUiState,
    onEntryNameChanged: (String) -> Unit,
    onEntryInputModeSelected: (EntryInputMode) -> Unit,
    onCalorieInputChanged: (String) -> Unit,
    onConsumedAmountInputChanged: (String) -> Unit,
    onCaloriesPer100InputChanged: (String) -> Unit,
    onTypeSelected: (CalorieEntryType) -> Unit,
    onSourceSelected: (CalorieEntrySource) -> Unit,
    onShowPreviousEntryDate: () -> Unit,
    onShowNextEntryDate: () -> Unit,
    onResetEntryDateToToday: () -> Unit,
    onSaveEntryClicked: () -> Unit,
    onCancelEditingClicked: () -> Unit,
    onShowDatePicker: () -> Unit,
    onAiMealDescriptionChanged: (String) -> Unit,
    onAnalyzeMealWithAi: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = trackerPanelColor(alpha = 0.82f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.showsMagicInput) {
                MagicInputSection(
                    input = uiState.aiMealDescriptionInput,
                    isAnalyzing = uiState.isAiAnalyzing,
                    error = uiState.aiAnalysisError,
                    onInputChanged = onAiMealDescriptionChanged,
                    onAnalyzeClicked = onAnalyzeMealWithAi
                )
            }

            OutlinedTextField(
                value = uiState.entryNameInput,
                onValueChange = onEntryNameChanged,
                label = { Text(stringResource(R.string.entry_name_input_label)) },
                supportingText = { Text(stringResource(R.string.entry_name_input_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            EntryDateSelector(
                uiState = uiState,
                onShowPreviousEntryDate = onShowPreviousEntryDate,
                onShowNextEntryDate = onShowNextEntryDate,
                onResetEntryDateToToday = onResetEntryDateToToday,
                onShowDatePicker = onShowDatePicker
            )

            SelectionGroup(
                title = stringResource(R.string.entry_input_mode_title),
                options = listOf(
                    SelectionOption(
                        label = stringResource(R.string.entry_input_mode_direct),
                        selected = uiState.usesDirectCalorieInput,
                        accent = Ink,
                        onClick = { onEntryInputModeSelected(EntryInputMode.DirectCalories) }
                    ),
                    SelectionOption(
                        label = stringResource(R.string.entry_input_mode_portion),
                        selected = uiState.usesPortionCalculator,
                        accent = Gold,
                        onClick = { onEntryInputModeSelected(EntryInputMode.PortionCalculator) }
                    )
                )
            )

            if (uiState.usesDirectCalorieInput) {
                OutlinedTextField(
                    value = uiState.calorieInput,
                    onValueChange = onCalorieInputChanged,
                    label = { Text(stringResource(R.string.calorie_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.inputError != null,
                    supportingText = {
                        Text(
                            calorieInputErrorMessage(uiState.inputError)
                                ?: stringResource(R.string.calorie_input_hint)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TrackerScreenTestTags.CALORIE_INPUT_FIELD)
                )
            } else {
                OutlinedTextField(
                    value = uiState.consumedAmountInput,
                    onValueChange = onConsumedAmountInputChanged,
                    label = { Text(stringResource(R.string.consumed_amount_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.consumedAmountInputError != null,
                    supportingText = {
                        Text(
                            calorieInputErrorMessage(uiState.consumedAmountInputError)
                                ?: stringResource(R.string.consumed_amount_input_hint)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.caloriesPer100Input,
                    onValueChange = onCaloriesPer100InputChanged,
                    label = { Text(stringResource(R.string.calories_per_100_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.caloriesPer100InputError != null,
                    supportingText = {
                        Text(
                            calorieInputErrorMessage(uiState.caloriesPer100InputError)
                                ?: stringResource(R.string.calories_per_100_input_hint)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = uiState.calculatedCaloriesPreview?.let { calculatedCalories ->
                        stringResource(
                            R.string.calculated_calories_preview_template,
                            calculatedCalories
                        )
                    } ?: stringResource(R.string.calculated_calories_preview_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = trackerSecondaryTextColor()
                )
            }

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

            if (uiState.showsManualTypePicker) {
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
                            accent = com.example.kalorientracker.ui.theme.Coral,
                            onClick = { onTypeSelected(CalorieEntryType.BURNED) }
                        )
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveEntryClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink,
                        contentColor = WhiteSmoke
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth(if (uiState.isEditing) 0.72f else 1f)
                        .testTag(TrackerScreenTestTags.ADD_ENTRY_BUTTON)
                ) {
                    Text(
                        text = stringResource(
                            if (uiState.isEditing) R.string.update_entry_button else R.string.add_entry_button
                        )
                    )
                }

                if (uiState.isEditing) {
                    TextButton(
                        onClick = onCancelEditingClicked,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(text = stringResource(R.string.cancel_edit_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryDateSelector(
    uiState: TrackerUiState,
    onShowPreviousEntryDate: () -> Unit,
    onShowNextEntryDate: () -> Unit,
    onResetEntryDateToToday: () -> Unit,
    onShowDatePicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.entry_date_label),
            style = MaterialTheme.typography.labelLarge,
            color = trackerSecondaryTextColor()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onShowPreviousEntryDate) {
                Text(text = stringResource(R.string.entry_date_previous))
            }
            Text(
                text = LocalDate.ofEpochDay(uiState.entryRecordedOnEpochDay).format(entryDateFormatter()),
                style = MaterialTheme.typography.bodyLarge,
                color = trackerPrimaryTextColor(),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowDatePicker() }
            )
            TextButton(
                onClick = onShowNextEntryDate,
                enabled = uiState.canMoveEntryDateForward
            ) {
                Text(text = stringResource(R.string.entry_date_next))
            }
        }
        TextButton(onClick = onResetEntryDateToToday) {
            Text(text = stringResource(R.string.entry_date_today))
        }
    }
}

@Composable
private fun MagicInputSection(
    input: String,
    isAnalyzing: Boolean,
    error: String?,
    onInputChanged: (String) -> Unit,
    onAnalyzeClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Magic Log (AI)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Describe your meal and let AI fill the form for you.",
                style = MaterialTheme.typography.bodySmall,
                color = trackerSecondaryTextColor()
            )

            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                label = { Text("e.g., A large apple and black coffee") },
                enabled = !isAnalyzing,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onAnalyzeClicked,
                enabled = input.isNotBlank() && !isAnalyzing,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Analyze")
                }
            }
        }
    }
}
