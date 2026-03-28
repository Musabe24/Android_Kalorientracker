package com.example.kalorientracker.data.calorie

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieEntryDao {
    @Query("SELECT * FROM calorie_entries ORDER BY recordedOnEpochDay ASC, id ASC")
    fun observeAll(): Flow<List<CalorieEntryEntity>>

    @Query("SELECT * FROM calorie_entries ORDER BY recordedOnEpochDay ASC, id ASC")
    suspend fun getAll(): List<CalorieEntryEntity>

    @Query(
        """
        SELECT * FROM calorie_entries
        WHERE recordedOnEpochDay BETWEEN :startEpochDayInclusive AND :endEpochDayInclusive
        ORDER BY recordedOnEpochDay ASC, id ASC
        """
    )
    fun observeBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): Flow<List<CalorieEntryEntity>>

    @Query(
        """
        SELECT * FROM calorie_entries
        WHERE recordedOnEpochDay BETWEEN :startEpochDayInclusive AND :endEpochDayInclusive
        ORDER BY recordedOnEpochDay ASC, id ASC
        """
    )
    suspend fun getBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CalorieEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<CalorieEntryEntity>)

    @Query("DELETE FROM calorie_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query("SELECT COUNT(*) FROM calorie_entries")
    suspend fun count(): Int
}
