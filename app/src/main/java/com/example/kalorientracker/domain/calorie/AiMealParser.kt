package com.example.kalorientracker.domain.calorie

interface AiMealParser {
    suspend fun parseMealDescription(
        description: String,
        model: SupportedAiModel
    ): AiMealAnalysisResult

    fun updateApiKey(apiKey: String)
}

enum class SupportedAiModel(val modelId: String, val displayName: String) {
    GEMINI_3_1_FLASH_LITE("gemini-3.1-flash-lite-preview", "Gemini 3.1 Lite")
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
