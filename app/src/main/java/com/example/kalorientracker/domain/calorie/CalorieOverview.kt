package com.example.kalorientracker.domain.calorie

data class CalorieOverview(
    val entries: List<CalorieEntry>,
    val summary: DailyCalorieSummary
)
