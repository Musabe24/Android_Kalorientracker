package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadCalorieHistoryUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator
) {
    fun observe(): Flow<List<CalorieHistoryDay>> {
        return repository.observeEntries().map(::buildHistoryDays)
    }

    suspend operator fun invoke(): List<CalorieHistoryDay> = buildHistoryDays(repository.getEntries())

    private fun buildHistoryDays(entries: List<CalorieEntry>): List<CalorieHistoryDay> {
        return entries
            .groupBy(CalorieEntry::recordedOnEpochDay)
            .toSortedMap(compareByDescending { it })
            .map { (epochDay, dayEntries) ->
                val summary = dailyCalorieCalculator.calculateSummary(dayEntries)
                CalorieHistoryDay(
                    epochDay = epochDay,
                    entries = dayEntries.sortedByDescending(CalorieEntry::id),
                    totalIntake = summary.totalIntake,
                    totalBurned = summary.totalBurned,
                    netCalories = summary.netCalories
                )
            }
    }
}
