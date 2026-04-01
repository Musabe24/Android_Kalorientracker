package com.example.kalorientracker.domain.calorie

interface AiMealParser {
    suspend fun parseMealDescription(description: String): AiMealAnalysisResult
}

sealed class AiMealAnalysisResult {
    data class Success(val meals: List<AiParsedMeal>) : AiMealAnalysisResult()
    data class Error(val message: String) : AiMealAnalysisResult()
}

data class AiParsedMeal(
    val name: String,
    val calories: Int,
    val type: CalorieEntryType,
    val source: CalorieEntrySource
)
