package com.example.goshmak.domain

import com.example.goshmak.domain.entity.GameSettings
import com.example.goshmak.domain.entity.Level
import com.example.goshmak.domain.entity.Question

interface GameRepository {

    fun generateQuestion(maxSumValue: Int, countOfOptions: Int): Question

    fun getGameSettings(level: Level): GameSettings
}