package com.example.kalorientracker.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kalorientracker.R
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.ui.theme.Coral
import com.example.kalorientracker.ui.theme.Ink
import com.example.kalorientracker.ui.theme.KalorientrackerTheme
import com.example.kalorientracker.ui.theme.Olive
import com.example.kalorientracker.ui.theme.Sky
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

object TrackerScreenTestTags {
    const val CALORIE_INPUT_FIELD = "calorie_input_field"
    const val ADD_ENTRY_BUTTON = "add_entry_button"
}

@Composable
fun TrackerScreen(viewModel: TrackerViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackerContent(
        uiState = uiState,
        onSelectDestination = viewModel::selectDestination,
        onEntryNameChanged = viewModel::onEntryNameChanged,
        onEntryInputModeSelected = viewModel::onEntryInputModeSelected,
        onCalorieInputChanged = viewModel::onCalorieInputChanged,
        onConsumedAmountInputChanged = viewModel::onConsumedAmountInputChanged,
        onCaloriesPer100InputChanged = viewModel::onCaloriesPer100InputChanged,
        onTypeSelected = viewModel::onEntryTypeSelected,
        onSourceSelected = viewModel::onEntrySourceSelected,
        onShowPreviousEntryDate = viewModel::showPreviousEntryDate,
        onShowNextEntryDate = viewModel::showNextEntryDate,
        onResetEntryDateToToday = viewModel::resetEntryDateToToday,
        onSaveEntryClicked = viewModel::saveEntry,
        onEditEntryClicked = viewModel::startEditing,
        onDeleteEntryClicked = viewModel::requestDeleteEntry,
        onCancelEditingClicked = viewModel::cancelEditing,
        onTrendRangeSelected = viewModel::selectTrendRange,
        onEarlierTrendWindowSelected = viewModel::showEarlierTrendWindow,
        onLaterTrendWindowSelected = viewModel::showLaterTrendWindow,
        onHistoryFilterSelected = viewModel::selectHistoryFilter,
        onDismissDeleteDialog = viewModel::dismissDeleteEntry,
        onConfirmDeleteEntry = viewModel::confirmDeleteEntry,
        onStartEditingGoalTarget = viewModel::startEditingGoalTarget,
        onGoalTargetInputChanged = viewModel::onGoalTargetInputChanged,
        onCancelGoalTargetEditing = viewModel::cancelGoalTargetEditing,
        onSaveGoalTarget = viewModel::saveGoalTarget,
        onShowDatePicker = viewModel::showDatePicker,
        onDismissDatePicker = viewModel::dismissDatePicker,
        onDateSelected = viewModel::onEntryDateSelected,
        onAiMealDescriptionChanged = viewModel::onAiMealDescriptionChanged,
        onAnalyzeMealWithAi = viewModel::analyzeMealWithAi,
        onAiApiKeyInputChanged = viewModel::onAiApiKeyInputChanged,
        onStartEditingAiSettings = viewModel::startEditingAiSettings,
        onCancelAiSettingsEditing = viewModel::cancelAiSettingsEditing,
        onSaveAiApiKey = viewModel::saveAiApiKey,
        modifier = modifier
    )
}

@Composable
fun TrackerContent(
    uiState: TrackerUiState,
    onSelectDestination: (TrackerDestination) -> Unit,
    onEntryNameChanged: (String) -> Unit,
    onEntryInputModeSelected: (EntryInputMode) -> Unit,
    onCalorieInputChanged: (String) -> Unit,
    onConsumedAmountInputChanged: (String) -> Unit,
    onCaloriesPer100InputChanged: (String) -> Unit,
    onTypeSelected: (com.example.kalorientracker.domain.calorie.CalorieEntryType) -> Unit,
    onSourceSelected: (com.example.kalorientracker.domain.calorie.CalorieEntrySource) -> Unit,
    onShowPreviousEntryDate: () -> Unit,
    onShowNextEntryDate: () -> Unit,
    onResetEntryDateToToday: () -> Unit,
    onSaveEntryClicked: () -> Unit,
    onEditEntryClicked: (CalorieEntry) -> Unit,
    onDeleteEntryClicked: (CalorieEntry) -> Unit,
    onCancelEditingClicked: () -> Unit,
    onTrendRangeSelected: (TrendRange) -> Unit,
    onEarlierTrendWindowSelected: () -> Unit,
    onLaterTrendWindowSelected: () -> Unit,
    onHistoryFilterSelected: (HistoryFilter) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteEntry: () -> Unit,
    onStartEditingGoalTarget: () -> Unit,
    onGoalTargetInputChanged: (String) -> Unit,
    onCancelGoalTargetEditing: () -> Unit,
    onSaveGoalTarget: () -> Unit,
    onShowDatePicker: () -> Unit,
    onDismissDatePicker: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onAiMealDescriptionChanged: (String) -> Unit,
    onAnalyzeMealWithAi: () -> Unit,
    onAiApiKeyInputChanged: (String) -> Unit,
    onStartEditingAiSettings: () -> Unit,
    onCancelAiSettingsEditing: () -> Unit,
    onSaveAiApiKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (uiState.selectedDestination) {
                TrackerDestination.Overview -> TrackerOverviewScreen(
                    uiState = uiState,
                    onTrendRangeSelected = onTrendRangeSelected,
                    onEarlierTrendWindowSelected = onEarlierTrendWindowSelected,
                    onLaterTrendWindowSelected = onLaterTrendWindowSelected,
                    onStartEditingGoalTarget = onStartEditingGoalTarget,
                    onGoalTargetInputChanged = onGoalTargetInputChanged,
                    onCancelGoalTargetEditing = onCancelGoalTargetEditing,
                    onSaveGoalTarget = onSaveGoalTarget,
                    onAiApiKeyInputChanged = onAiApiKeyInputChanged,
                    onStartEditingAiSettings = onStartEditingAiSettings,
                    onCancelAiSettingsEditing = onCancelAiSettingsEditing,
                    onSaveAiApiKey = onSaveAiApiKey,
                    contentPadding = PaddingValues(20.dp)
                )

                TrackerDestination.Capture -> TrackerCaptureScreen(
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
                    onEditEntryClicked = onEditEntryClicked,
                    onDeleteEntryClicked = onDeleteEntryClicked,
                    onShowDatePicker = onShowDatePicker,
                    onAiMealDescriptionChanged = onAiMealDescriptionChanged,
                    onAnalyzeMealWithAi = onAnalyzeMealWithAi,
                    contentPadding = PaddingValues(20.dp)
                )

                TrackerDestination.History -> TrackerHistoryScreen(
                    uiState = uiState,
                    onHistoryFilterSelected = onHistoryFilterSelected,
                    onEditEntryClicked = onEditEntryClicked,
                    onDeleteEntryClicked = onDeleteEntryClicked,
                    contentPadding = PaddingValues(20.dp)
                )
            }
        }

        TrackerBottomNavigation(
            selectedDestination = uiState.selectedDestination,
            onSelectDestination = onSelectDestination
        )
    }

    if (uiState.isDatePickerVisible) {
        TrackerDatePickerDialog(
            initialEpochDay = uiState.entryRecordedOnEpochDay,
            maxEpochDay = uiState.currentEpochDay,
            onDismiss = onDismissDatePicker,
            onDateSelected = {
                onDateSelected(it)
                onDismissDatePicker()
            }
        )
    }

    uiState.pendingDeleteEntry?.let { entry ->
        DeleteEntryDialog(
            entry = entry,
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDeleteEntry
        )
    }
}

@Composable
private fun TrackerBottomNavigation(
    selectedDestination: TrackerDestination,
    onSelectDestination: (TrackerDestination) -> Unit
) {
    val items = listOf(
        NavigationItem(
            destination = TrackerDestination.Overview,
            label = stringResource(R.string.navigation_overview),
            accent = Olive
        ),
        NavigationItem(
            destination = TrackerDestination.Capture,
            label = stringResource(R.string.navigation_capture),
            accent = Sky
        ),
        NavigationItem(
            destination = TrackerDestination.History,
            label = stringResource(R.string.navigation_history),
            accent = Coral
        )
    )

    Surface(shadowElevation = 12.dp) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = selectedDestination == item.destination,
                    onClick = { onSelectDestination(item.destination) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(item.accent)
                        )
                    },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = Ink,
                        indicatorColor = item.accent.copy(alpha = 0.18f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DeleteEntryDialog(
    entry: CalorieEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_dialog_title)) },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_dialog_message,
                    deleteEntryLabel(entry),
                    entry.amount
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Coral)
            ) {
                Text(text = stringResource(R.string.delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.delete_dialog_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackerDatePickerDialog(
    initialEpochDay: Long,
    maxEpochDay: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val initialMillis = LocalDate.ofEpochDay(initialEpochDay)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val maxMillis = LocalDate.ofEpochDay(maxEpochDay)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
                return utcTimeMillis <= maxMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedDate.toEpochDay())
                    }
                }
            ) {
                Text(stringResource(R.string.goal_target_save_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.goal_target_cancel_button))
            }
        },
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        DatePicker(state = datePickerState)
    }
}

private data class NavigationItem(
    val destination: TrackerDestination,
    val label: String,
    val accent: androidx.compose.ui.graphics.Color
)
