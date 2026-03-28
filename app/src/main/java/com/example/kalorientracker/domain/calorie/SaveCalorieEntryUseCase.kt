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
    suspend operator fun invoke(
        rawName: String,
        rawCalories: String,
        entryType: CalorieEntryType,
        entrySource: CalorieEntrySource,
        editingEntryId: String?,
        recordedOnEpochDay: Long
    ): SaveCalorieEntryResult {
        require(recordedOnEpochDay <= LocalDate.now(clock).toEpochDay()) {
            "Entries cannot be saved in the future."
        }

        return when (val validationResult = inputValidator.validate(rawCalories)) {
            is ValidationResult.Invalid -> SaveCalorieEntryResult.ValidationError(validationResult.reason)
            is ValidationResult.Valid -> {
                repository.saveEntry(
                    CalorieEntry(
                        id = editingEntryId ?: UUID.randomUUID().toString(),
                        name = rawName.trim(),
                        amount = validationResult.calories,
                        type = entryType,
                        source = entrySource,
                        recordedOnEpochDay = recordedOnEpochDay
                    )
                )
                SaveCalorieEntryResult.Success
            }
        }
    }
}

sealed interface SaveCalorieEntryResult {
    data object Success : SaveCalorieEntryResult

    data class ValidationError(val reason: CalorieInputValidationError) : SaveCalorieEntryResult
}
