package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.flow.Flow

/**
 * Stores the user's daily calorie target as a single persisted setting.
 *
 * Implementations must always return a positive calorie value and persist updates deterministically.
 */
interface GoalTargetRepository {
    fun observeTargetCalories(): Flow<Int>

    suspend fun getTargetCalories(): Int

    suspend fun setTargetCalories(targetCalories: Int)
}
