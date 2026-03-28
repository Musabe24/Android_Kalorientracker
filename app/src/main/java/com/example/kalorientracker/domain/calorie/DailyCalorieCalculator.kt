package com.example.kalorientracker.domain.calorie

/**
 * Calculates daily calorie totals and net balance.
 */
class DailyCalorieCalculator {
    fun calculateSummary(entries: List<CalorieEntry>): DailyCalorieSummary {
        require(entries.all { it.amount > 0 }) {
            "Calories must be greater than zero."
        }

        val totalIntake = entries
            .asSequence()
            .filter { it.type == CalorieEntryType.INTAKE }
            .sumOf { it.amount }

        val totalBurned = entries
            .asSequence()
            .filter { it.type == CalorieEntryType.BURNED }
            .sumOf { it.amount }

        return DailyCalorieSummary(
            totalIntake = totalIntake,
            totalBurned = totalBurned,
            netCalories = totalIntake - totalBurned
        )
    }
}
