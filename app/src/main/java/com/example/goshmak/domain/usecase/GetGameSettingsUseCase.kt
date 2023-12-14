package com.example.goshmak.domain.usecase

import com.example.goshmak.domain.GameRepository
import com.example.goshmak.domain.entity.GameSettings
import com.example.goshmak.domain.entity.Level

class GetGameSettingsUseCase(
    private val repository: GameRepository
) {
    operator fun invoke(level: Level): GameSettings{
        return repository.getGameSettings(level)
    }
}