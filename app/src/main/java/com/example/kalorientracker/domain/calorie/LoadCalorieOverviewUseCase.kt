package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Loads the current day's entries together with the derived daily summary.
 */
class LoadCalorieOverviewUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator,
    private val clock: Clock
) {
    fun observe(): Flow<CalorieOverview> = repository.observeEntries().map(::buildOverview)

    suspend operator fun invoke(): CalorieOverview = buildOverview(repository.getEntries())

    private fun buildOverview(entries: List<CalorieEntry>): CalorieOverview {
        val currentEpochDay = LocalDate.now(clock).toEpochDay()
        val todayEntries = entries.filter { it.recordedOnEpochDay == currentEpochDay }
        val summary = dailyCalorieCalculator.calculateSummary(todayEntries)
        return CalorieOverview(entries = todayEntries, summary = summary)
    }
}

data class CalorieOverview(
    val entries: List<CalorieEntry>,
    val summary: DailyCalorieSummary
)
