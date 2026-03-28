package com.example.kalorientracker.domain.calorie

/**
 * Represents a calorie entry tracked by the user.
 *
 * Contract:
 * - [amount] must be strictly positive.
 * - Intake and burned calories are differentiated by [type].
 */
data class CalorieEntry(
    val amount: Int,
    val type: CalorieEntryType,
    val source: CalorieEntrySource = CalorieEntrySource.MANUAL
)

enum class CalorieEntryType {
    INTAKE,
    BURNED
}

enum class CalorieEntrySource {
    MEAL,
    WATCH,
    MANUAL
}

data class DailyCalorieSummary(
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)

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
