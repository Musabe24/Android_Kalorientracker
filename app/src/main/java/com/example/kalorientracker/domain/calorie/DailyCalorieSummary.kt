package com.example.kalorientracker.domain.calorie

data class DailyCalorieSummary(
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)
