package com.example.kalorientracker.domain.calorie

/**
 * Repository contract for tracked calorie entries.
 *
 * Failure behavior:
 * - Implementations must either persist the full entry list or fail explicitly.
 * - Implementations must not silently ignore malformed or failed writes.
 */
interface CalorieEntryRepository {
    fun getEntries(): List<CalorieEntry>

    fun saveEntry(entry: CalorieEntry)

    fun deleteEntry(entryId: String)
}
