package com.example.kalorientracker.data.calorie

import android.content.SharedPreferences
import com.example.kalorientracker.domain.calorie.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesSettingsRepository(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    private val _apiKeyFlow = MutableStateFlow(loadApiKey())
    
    override fun getAiApiKey(): Flow<String?> = _apiKeyFlow.asStateFlow()

    override suspend fun saveAiApiKey(apiKey: String) {
        val trimmedKey = apiKey.trim()
        sharedPreferences.edit().putString(KEY_AI_API_KEY, trimmedKey).apply()
        _apiKeyFlow.value = trimmedKey
    }

    private fun loadApiKey(): String? {
        return sharedPreferences.getString(KEY_AI_API_KEY, null)
    }

    private companion object {
        const val KEY_AI_API_KEY = "ai_api_key"
    }
}
