package com.example.kalorientracker.domain.calorie

data class MealEntry(val calories: Int)

/**
 * Calculates total calories for a day.
 *
 * Contract:
 * - Every [MealEntry.calories] value must be zero or positive.
 * - Throws [IllegalArgumentException] when any entry is negative.
 */
class DailyCalorieCalculator {
    fun totalCalories(entries: List<MealEntry>): Int {
        require(entries.none { it.calories < 0 }) {
            "Calories must be zero or positive."
        }

        return entries.sumOf { it.calories }
    }
}
