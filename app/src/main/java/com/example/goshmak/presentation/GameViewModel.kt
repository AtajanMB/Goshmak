package com.example.goshmak.presentation

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.goshmak.R
import com.example.goshmak.data.GameRepositoryImpl
import com.example.goshmak.domain.entity.GameResult
import com.example.goshmak.domain.entity.GameSettings
import com.example.goshmak.domain.entity.Level
import com.example.goshmak.domain.entity.Question
import com.example.goshmak.domain.usecase.GenerateQuestionUseCase
import com.example.goshmak.domain.usecase.GetGameSettingsUseCase

class GameViewModel(
    private val level: Level,
    private val application: Application
) : ViewModel() {

    private val repository = GameRepositoryImpl
    private val generateQuestionUseCase = GenerateQuestionUseCase(repository)
    private val getGameSettingsUseCase = GetGameSettingsUseCase(repository)
    private lateinit var gameSettings: GameSettings
    private var timer: CountDownTimer? = null

    private val _countDown = MutableLiveData<String>()
    val countDown: LiveData<String>
        get() = _countDown

    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question>
        get() = _question

    private val _percentOfRightAnswers = MutableLiveData<Int>()
    val percentOfRightAnswers: LiveData<Int>
        get() = _percentOfRightAnswers

    private val _progressAnswers = MutableLiveData<String>()
    val progressAnswers: LiveData<String>
        get() = _progressAnswers

    private val _enoughCount = MutableLiveData<Boolean>()
    val enoughCount: LiveData<Boolean>
        get() = _enoughCount

    private val _enoughPercent = MutableLiveData<Boolean>()
    val enoughPercent: LiveData<Boolean>
        get() = _enoughPercent

    private val _gameResult = MutableLiveData<GameResult>()
    val gameResult: LiveData<GameResult>
        get() = _gameResult

    private val _minPercent = MutableLiveData<Int>()
    val minPercent: LiveData<Int>
        get() = _minPercent

    private var countOfRightAnswers = 0
    private var countOfQuestions = 0


    init {
        startGame()
    }

    private fun startGame() {
        this.gameSettings = getGameSettingsUseCase(level)
        startTimer()
        generateQuestion()
        updateProgress()
    }

    fun chooseAnswer(number: Int) {
        checkAnswer(number)
        updateProgress()
        generateQuestion()
    }

    private fun checkAnswer(number: Int) {
        val rightAnswer = question.value?.rightAnswer
        if (number == rightAnswer) {
            countOfRightAnswers++
        }
        countOfQuestions++
    }

    private fun updateProgress() {
        val percent = calculatePercentOfRightAnswers()
        _percentOfRightAnswers.value = percent
        _progressAnswers.value = String.format(
            application.resources
                .getString(R.string.progress_answers),
            countOfRightAnswers,
            gameSettings.minCountOfRightAnswers
        )
        _enoughCount.value = countOfRightAnswers >= gameSettings.minCountOfRightAnswers
        _enoughPercent.value = percent >= gameSettings.minPercentOfRightAnswers
    }

    private fun calculatePercentOfRightAnswers(): Int {
        if (countOfQuestions == 0) {
            return 0
        }
        return ((countOfRightAnswers / countOfQuestions.toDouble()) * 100).toInt()
    }

    private fun generateQuestion() {
        _question.value = generateQuestionUseCase(gameSettings.maxSumValue)
        _minPercent.value = gameSettings.minPercentOfRightAnswers
    }

    private fun startTimer() {
        timer = object :
            CountDownTimer(gameSettings.gameTimeInSeconds * SECOND_IN_MILLIS, SECOND_IN_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                _countDown.value = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                finishGame()
            }
        }
        timer?.start()
    }

    private fun finishGame() {
        _gameResult.value = GameResult(
            enoughCount.value == true && enoughPercent.value == true,
            countOfRightAnswers,
            countOfQuestions,
            gameSettings
        )
    }

    private fun formatTime(millisUntilFinished: Long): String {
        val seconds = millisUntilFinished / SECOND_IN_MILLIS
        val minutes = seconds / MINUTES_IN_SECONDS
        val leftSeconds = seconds - (minutes * MINUTES_IN_SECONDS)
        return String.format("%02d:%02d", minutes, leftSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    companion object {
        const val SECOND_IN_MILLIS = 1000L
        const val MINUTES_IN_SECONDS = 60
    }
}