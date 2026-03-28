package com.example.kalorientracker.domain.calorie

import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadWeeklyCalorieTrendUseCase(
    private val repository: CalorieEntryRepository,
    private val dailyCalorieCalculator: DailyCalorieCalculator,
    private val clock: Clock
) {
    fun observe(days: Int = 7): Flow<List<DailyCalorieTrendPoint>> {
        require(days > 0) { "Days must be greater than zero." }
        return repository.observeEntries().map { entries ->
            buildWeeklyTrend(entries = entries, days = days)
        }
    }

    suspend operator fun invoke(days: Int = 7): List<DailyCalorieTrendPoint> {
        require(days > 0) { "Days must be greater than zero." }

        return buildWeeklyTrend(entries = repository.getEntries(), days = days)
    }

    private fun buildWeeklyTrend(
        entries: List<CalorieEntry>,
        days: Int
    ): List<DailyCalorieTrendPoint> {
        val endDate = LocalDate.now(clock)
        val startDate = endDate.minusDays(days.toLong() - 1L)
        val visibleEntries = entries.filter {
            it.recordedOnEpochDay in startDate.toEpochDay()..endDate.toEpochDay()
        }
        val entriesByDay = visibleEntries.groupBy(CalorieEntry::recordedOnEpochDay)

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
