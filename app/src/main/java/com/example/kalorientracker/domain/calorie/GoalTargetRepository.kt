package com.example.kalorientracker.domain.calorie

/**
 * Stores the user's daily calorie target as a single persisted setting.
 *
 * Implementations must always return a positive calorie value and persist updates deterministically.
 */
interface GoalTargetRepository {
    suspend fun getTargetCalories(): Int

    suspend fun setTargetCalories(targetCalories: Int)
}
