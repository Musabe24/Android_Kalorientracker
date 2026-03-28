package com.example.kalorientracker.data.calorie

import android.content.SharedPreferences
import com.example.kalorientracker.domain.calorie.CalorieEntry

/**
 * Reads legacy SharedPreferences entries so they can be migrated into Room once.
 */
class LegacyCalorieEntryStore(
    private val sharedPreferences: SharedPreferences,
    private val storageCodec: CalorieEntryStorageCodec
) {
    fun readEntries(): List<CalorieEntry> {
        val serializedEntries = sharedPreferences.getString(ENTRIES_KEY, null).orEmpty()
        return storageCodec.decode(serializedEntries)
    }

    fun clear() {
        sharedPreferences.edit().remove(ENTRIES_KEY).commit()
    }

    private companion object {
        const val ENTRIES_KEY = "tracked_calorie_entries"
    }
}
