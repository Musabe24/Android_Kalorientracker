package com.example.kalorientracker.domain.calorie

class AnalyzeMealUseCase(
    val aiMealParser: AiMealParser
) {
    suspend operator fun invoke(
        description: String,
        model: SupportedAiModel = SupportedAiModel.GEMINI_3_1_FLASH_LITE
    ): AiMealAnalysisResult {
        if (description.isBlank()) {
            return AiMealAnalysisResult.Error("Description cannot be blank")
        }
        return aiMealParser.parseMealDescription(description, model)
    }
}
