package com.example.kalorientracker.domain.calorie

class CalculateGoalProgressUseCase {
    operator fun invoke(
        netCalories: Int,
        weeklyTrend: List<DailyCalorieTrendPoint>,
        targetCalories: Int = DEFAULT_TARGET_CALORIES
    ): GoalProgressInsights {
        require(targetCalories > 0) { "Target calories must be greater than zero." }

        val remainingCalories = targetCalories - netCalories
        val progressRatio = (netCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
        val activeTrendDays = weeklyTrend.filter { it.totalIntake > 0 || it.totalBurned > 0 }
        val averageNetCalories = if (activeTrendDays.isEmpty()) {
            0
        } else {
            activeTrendDays.sumOf(DailyCalorieTrendPoint::netCalories) / activeTrendDays.size
        }
        val hitDays = activeTrendDays.count { it.netCalories in 1..targetCalories }

        return GoalProgressInsights(
            targetCalories = targetCalories,
            remainingCalories = remainingCalories,
            progressRatio = progressRatio,
            averageNetCalories = averageNetCalories,
            targetHitDays = hitDays,
            consistencyStreak = calculateConsistencyStreak(
                weeklyTrend = weeklyTrend,
                targetCalories = targetCalories
            )
        )
    }

    private fun calculateConsistencyStreak(
        weeklyTrend: List<DailyCalorieTrendPoint>,
        targetCalories: Int
    ): Int {
        var streak = 0
        for (point in weeklyTrend.asReversed()) {
            val hasActivity = point.totalIntake > 0 || point.totalBurned > 0
            if (!hasActivity || point.netCalories !in 1..targetCalories) {
                break
            }
            streak += 1
        }
        return streak
    }

    companion object {
        const val DEFAULT_TARGET_CALORIES = 2200
    }
}

data class GoalProgressInsights(
    val targetCalories: Int,
    val remainingCalories: Int,
    val progressRatio: Float,
    val averageNetCalories: Int,
    val targetHitDays: Int,
    val consistencyStreak: Int
)
