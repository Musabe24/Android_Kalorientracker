package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.LocalDate

class LoadWeeklyCalorieTrendUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator,
    private val clock: Clock
) {
    suspend operator fun invoke(days: Int = 7): List<DailyCalorieTrendPoint> {
        require(days > 0) { "Days must be greater than zero." }

        val endDate = LocalDate.now(clock)
        val startDate = endDate.minusDays(days.toLong() - 1L)
        val entries = repository.getEntriesBetween(
            startEpochDayInclusive = startDate.toEpochDay(),
            endEpochDayInclusive = endDate.toEpochDay()
        )

        val entriesByDay = entries.groupBy(CalorieEntry::recordedOnEpochDay)

        return (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val dayEntries = entriesByDay[date.toEpochDay()].orEmpty()
            val summary = dailyCalorieCalculator.calculateSummary(dayEntries)
            DailyCalorieTrendPoint(
                epochDay = date.toEpochDay(),
                totalIntake = summary.totalIntake,
                totalBurned = summary.totalBurned,
                netCalories = summary.netCalories
            )
        }
    }
}

data class DailyCalorieTrendPoint(
    val epochDay: Long,
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)
