package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RoomCalorieEntryRepository(
    private val calorieEntryDao: CalorieEntryDao,
    private val legacyCalorieEntryStore: LegacyCalorieEntryStore
) : CalorieEntryRepository {
    override fun observeEntries(): Flow<List<CalorieEntry>> = flow {
        migrateLegacyEntriesIfNeeded()
        emitAll(
            calorieEntryDao.observeAll().map { entities ->
                entities.map(CalorieEntryEntity::toDomain)
            }
        )
    }

    override suspend fun getEntries(): List<CalorieEntry> {
        migrateLegacyEntriesIfNeeded()
        return calorieEntryDao.getAll().map(CalorieEntryEntity::toDomain)
    }

    override suspend fun getEntriesBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntry> {
        migrateLegacyEntriesIfNeeded()
        return calorieEntryDao.getBetween(
            startEpochDayInclusive = startEpochDayInclusive,
            endEpochDayInclusive = endEpochDayInclusive
        ).map(CalorieEntryEntity::toDomain)
    }

    override suspend fun saveEntry(entry: CalorieEntry) {
        migrateLegacyEntriesIfNeeded()
        calorieEntryDao.upsert(entry.toEntity())
    }

    override suspend fun deleteEntry(entryId: String) {
        migrateLegacyEntriesIfNeeded()
        calorieEntryDao.deleteById(entryId)
    }

    private suspend fun migrateLegacyEntriesIfNeeded() {
        if (calorieEntryDao.count() > 0) {
            return
        }

        val legacyEntries = legacyCalorieEntryStore.readEntries()
        if (legacyEntries.isEmpty()) {
            return
        }

        calorieEntryDao.upsertAll(legacyEntries.map(CalorieEntry::toEntity))
        legacyCalorieEntryStore.clear()
    }
}
