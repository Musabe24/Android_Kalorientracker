package com.example.kalorientracker.domain.calorie

data class CalorieHistoryDay(
    val epochDay: Long,
    val entries: List<CalorieEntry>,
    val totalIntake: Int,
    val totalBurned: Int,
    val netCalories: Int
)
