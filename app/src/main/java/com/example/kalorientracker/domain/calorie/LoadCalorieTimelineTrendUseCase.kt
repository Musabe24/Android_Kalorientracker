package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.LocalDate

class LoadCalorieTimelineTrendUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator,
    private val clock: Clock
) {
    suspend operator fun invoke(): List<DailyCalorieTrendPoint> {
        val entries = repository.getEntries()
        if (entries.isEmpty()) {
            return emptyList()
        }

        val entriesByDay = entries.groupBy(CalorieEntry::recordedOnEpochDay)
        val earliestEpochDay = entries.minOf(CalorieEntry::recordedOnEpochDay)
        val lastEpochDay = LocalDate.now(clock).toEpochDay()

        return (earliestEpochDay..lastEpochDay).map { epochDay ->
            val dayEntries = entriesByDay[epochDay].orEmpty()
            val summary = dailyCalorieCalculator.calculateSummary(dayEntries)
            DailyCalorieTrendPoint(
                epochDay = epochDay,
                totalIntake = summary.totalIntake,
                totalBurned = summary.totalBurned,
                netCalories = summary.netCalories
            )
        }
    }
}
