package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAiApiKey(): Flow<String?>
    suspend fun saveAiApiKey(apiKey: String)
}
