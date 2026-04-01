package com.example.kalorientracker.data.calorie

import com.example.kalorientracker.domain.calorie.AiMealAnalysisResult
import com.example.kalorientracker.domain.calorie.AiMealParser
import com.example.kalorientracker.domain.calorie.AiParsedMeal
import com.example.kalorientracker.domain.calorie.CalorieEntrySource
import com.example.kalorientracker.domain.calorie.CalorieEntryType
import com.example.kalorientracker.domain.calorie.SupportedAiModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class GeminiAiMealParser(
    private var apiKey: String
) : AiMealParser {

    override fun updateApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    private val systemInstructionText =
        "You are a nutritional assistant. Your task is to analyze meal descriptions and extract food items. " +
        "For each item, identify: 'name', 'calories' (integer estimate), 'type' (either 'INTAKE' for food/drinks or 'BURNED' for activities), " +
        "and 'source' (either 'MEAL' for food, 'WATCH' for exercise, or 'MANUAL' if unsure). " +
        "Respond with a JSON array of objects following this structure: " +
        "[{\"name\": \"Food name\", \"calories\": 250, \"type\": \"INTAKE\", \"source\": \"MEAL\"}]"

    override suspend fun parseMealDescription(
        description: String,
        model: SupportedAiModel
    ): AiMealAnalysisResult = withContext(Dispatchers.IO) {
        try {
            val generativeModel = GenerativeModel(
                modelName = model.modelId,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                },
                systemInstruction = content { text(systemInstructionText) }
            )

            val response = generativeModel.generateContent(description)
            val jsonString = response.text ?: return@withContext AiMealAnalysisResult.Error("No response from AI")
// ... (rest of method)
            val jsonArray = JSONArray(jsonString)
            val meals = mutableListOf<AiParsedMeal>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                meals.add(
                    AiParsedMeal(
                        name = obj.getString("name"),
                        calories = obj.getInt("calories"),
                        type = CalorieEntryType.valueOf(obj.getString("type")),
                        source = CalorieEntrySource.valueOf(obj.getString("source"))
                    )
                )
            }
            
            AiMealAnalysisResult.Success(meals)
        } catch (e: Exception) {
            AiMealAnalysisResult.Error(e.message ?: "Unknown error during AI analysis")
        }
    }
}
