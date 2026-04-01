package com.example.kalorientracker.domain.calorie

class AnalyzeMealUseCase(
    private val aiMealParser: AiMealParser
) {
    suspend operator fun invoke(description: String): AiMealAnalysisResult {
        if (description.isBlank()) {
            return AiMealAnalysisResult.Error("Description cannot be blank")
        }
        return aiMealParser.parseMealDescription(description)
    }
}
