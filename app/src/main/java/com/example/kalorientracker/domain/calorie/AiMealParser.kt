package com.example.kalorientracker.domain.calorie

interface AiMealParser {
    suspend fun parseMealDescription(
        description: String,
        model: SupportedAiModel
    ): AiMealAnalysisResult
}

enum class SupportedAiModel(val modelId: String, val displayName: String) {
    GEMINI_1_5_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash (Fast)"),
    GEMINI_1_5_PRO("gemini-1.5-pro", "Gemini 1.5 Pro (Complex)")
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
