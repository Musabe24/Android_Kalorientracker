package com.example.kalorientracker.domain.calorie

class LoadCalorieHistoryUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator
) {
    suspend operator fun invoke(): List<CalorieHistoryDay> {
        val entries = repository.getEntries()

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

data class CalorieHistoryDay(
    val epochDay: Long,
    val entries: List<CalorieEntry>,
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)
