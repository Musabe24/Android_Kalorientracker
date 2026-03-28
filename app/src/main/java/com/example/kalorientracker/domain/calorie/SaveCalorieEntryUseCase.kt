package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.LocalDate
import java.util.UUID

/**
 * Validates and persists a calorie entry.
 *
 * Existing entries keep their identity and recorded day when edited.
 */
class SaveCalorieEntryUseCase(
    private val repository: CalorieEntryRepository,
    private val inputValidator: CalorieInputValidator,
    private val clock: Clock
) {
    operator fun invoke(
        rawCalories: String,
        entryType: CalorieEntryType,
        entrySource: CalorieEntrySource,
        editingEntryId: String?,
        editingEntryRecordedOnEpochDay: Long?
    ): SaveCalorieEntryResult {
        require((editingEntryId == null) == (editingEntryRecordedOnEpochDay == null)) {
            "Editing metadata must contain both id and recorded day or neither."
        }

        return when (val validationResult = inputValidator.validate(rawCalories)) {
            is ValidationResult.Invalid -> SaveCalorieEntryResult.ValidationError(validationResult.reason)
            is ValidationResult.Valid -> {
                repository.saveEntry(
                    CalorieEntry(
                        id = editingEntryId ?: UUID.randomUUID().toString(),
                        amount = validationResult.calories,
                        type = entryType,
                        source = entrySource,
                        recordedOnEpochDay = editingEntryRecordedOnEpochDay
                            ?: LocalDate.now(clock).toEpochDay()
                    )
                )
                SaveCalorieEntryResult.Success
            }
        }
    }
}

sealed interface SaveCalorieEntryResult {
    data object Success : SaveCalorieEntryResult

    data class ValidationError(val message: String) : SaveCalorieEntryResult
}
