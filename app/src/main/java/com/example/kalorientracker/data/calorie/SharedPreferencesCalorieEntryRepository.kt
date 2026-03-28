package com.example.kalorientracker.data.calorie

import android.content.SharedPreferences
import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository

/**
 * Stores calorie entries as a single serialized payload in SharedPreferences.
 */
class SharedPreferencesCalorieEntryRepository(
    private val sharedPreferences: SharedPreferences,
    private val storageCodec: CalorieEntryStorageCodec
) : CalorieEntryRepository {

    override fun getEntries(): List<CalorieEntry> {
        val serializedEntries = sharedPreferences.getString(ENTRIES_KEY, null).orEmpty()
        return storageCodec.decode(serializedEntries)
    }

    override fun saveEntry(entry: CalorieEntry) {
        val existingEntries = getEntries()
        val entryIndex = existingEntries.indexOfFirst { it.id == entry.id }
        val updatedEntries = if (entryIndex >= 0) {
            existingEntries.toMutableList().apply {
                set(entryIndex, entry)
            }
        } else {
            existingEntries + entry
        }
        persistEntries(updatedEntries)
    }

    override fun deleteEntry(entryId: String) {
        val updatedEntries = getEntries().filterNot { it.id == entryId }
        persistEntries(updatedEntries)
    }

    private fun persistEntries(entries: List<CalorieEntry>) {
        val didPersist = sharedPreferences
            .edit()
            .putString(ENTRIES_KEY, storageCodec.encode(entries))
            .commit()

        check(didPersist) {
            "Failed to persist calorie entries."
        }
    }

    private companion object {
        const val ENTRIES_KEY = "tracked_calorie_entries"
    }
}
