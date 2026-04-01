package com.example.kalorientracker.domain.calorie

import kotlinx.coroutines.flow.Flow

class LoadAiApiKeyUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<String?> = repository.getAiApiKey()
}
