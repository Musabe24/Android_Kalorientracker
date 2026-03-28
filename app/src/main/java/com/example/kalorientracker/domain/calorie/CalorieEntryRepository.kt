package com.example.kalorientracker.domain.calorie

/**
 * Repository contract for tracked calorie entries.
 *
 * Failure behavior:
 * - Implementations must either persist the full entry list or fail explicitly.
 * - Implementations must not silently ignore malformed or failed writes.
 */
interface CalorieEntryRepository {
    suspend fun getEntries(): List<CalorieEntry>

    suspend fun getEntriesBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntry>

    suspend fun saveEntry(entry: CalorieEntry)

    suspend fun deleteEntry(entryId: String)
}
