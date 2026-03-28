package com.example.kalorientracker.domain.calorie

data class GoalProgressInsights(
    val targetCalories: Int,
    val remainingCalories: Int,
    val progressRatio: Float,
    val averageNetCalories: Int,
    val targetHitDays: Int,
    val consistencyStreak: Int
)
