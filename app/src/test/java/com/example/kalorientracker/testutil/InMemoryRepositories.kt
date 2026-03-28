package com.example.kalorientracker.testutil

import com.example.kalorientracker.domain.calorie.CalorieEntry
import com.example.kalorientracker.domain.calorie.CalorieEntryRepository
import com.example.kalorientracker.domain.calorie.CalculateGoalProgressUseCase
import com.example.kalorientracker.domain.calorie.GoalTargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class InMemoryCalorieEntryRepository(
    initialEntries: List<CalorieEntry> = emptyList()
) : CalorieEntryRepository {
    private val entries = initialEntries.toMutableList()
    private val entriesFlow = MutableStateFlow(entries.toList())

    override fun observeEntries(): Flow<List<CalorieEntry>> = entriesFlow.asStateFlow()

    override suspend fun getEntries(): List<CalorieEntry> = entries.toList()

    override suspend fun getEntriesBetween(
        startEpochDayInclusive: Long,
        endEpochDayInclusive: Long
    ): List<CalorieEntry> {
        return entries.filter {
            it.recordedOnEpochDay in startEpochDayInclusive..endEpochDayInclusive
        }
    }

    override suspend fun saveEntry(entry: CalorieEntry) {
        val existingIndex = entries.indexOfFirst { it.id == entry.id }
        if (existingIndex >= 0) {
            entries[existingIndex] = entry
        } else {
            entries += entry
        }
        entriesFlow.value = entries.toList()
    }

    override suspend fun deleteEntry(entryId: String) {
        entries.removeAll { it.id == entryId }
        entriesFlow.value = entries.toList()
    }
}

open class InMemoryGoalTargetRepository(
    initialTargetCalories: Int = CalculateGoalProgressUseCase.DEFAULT_TARGET_CALORIES
) : GoalTargetRepository {
    private var targetCalories = initialTargetCalories
    private val targetFlow = MutableStateFlow(initialTargetCalories)

    override fun observeTargetCalories(): Flow<Int> = targetFlow.asStateFlow()

    override suspend fun getTargetCalories(): Int = targetCalories

    override suspend fun setTargetCalories(targetCalories: Int) {
        this.targetCalories = targetCalories
        targetFlow.value = targetCalories
    }
}
