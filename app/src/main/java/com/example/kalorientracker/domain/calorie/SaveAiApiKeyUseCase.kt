package com.example.kalorientracker.domain.calorie

class SaveAiApiKeyUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(apiKey: String) {
        repository.saveAiApiKey(apiKey)
    }
}
