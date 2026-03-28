package com.example.kalorientracker.domain.calorie

data class DailyCalorieTrendPoint(
    val epochDay: Long,
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)
