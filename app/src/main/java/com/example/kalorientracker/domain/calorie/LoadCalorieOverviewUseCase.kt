package com.example.kalorientracker.domain.calorie

/**
 * Loads all persisted entries together with the derived daily summary.
 */
class LoadCalorieOverviewUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator
) {
    operator fun invoke(): CalorieOverview {
        val entries = repository.getEntries()
        val summary = dailyCalorieCalculator.calculateSummary(entries)
        return CalorieOverview(
            entries = entries,
            summary = summary
        )
    }
}

data class CalorieOverview(
    val entries: List<CalorieEntry>,
    val summary: DailyCalorieSummary
)
