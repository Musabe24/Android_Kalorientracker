package com.example.kalorientracker.domain.calorie

/**
 * Removes a persisted calorie entry by its stable identifier.
 */
class DeleteCalorieEntryUseCase(
    private val repository: CalorieEntryRepository
) {
    suspend operator fun invoke(entryId: String) {
        repository.deleteEntry(entryId)
    }
}
