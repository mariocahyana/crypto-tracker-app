package com.example.gamestorehb.ui.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.usecase.CalculateRiskProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiskProfileViewModel @Inject constructor(
    private val calculateRiskProfile: CalculateRiskProfileUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _answers = MutableStateFlow<List<Int>>(emptyList())
    val answers: StateFlow<List<Int>> = _answers.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    val questions = listOf(
        "How would you react if your portfolio lost 20% of its value in a week?",
        "What is your primary goal for this investment?",
        "How long do you plan to hold your investments before selling?"
    )

    val options = listOf(
        listOf("Sell immediately to prevent further loss", "Wait a few months to see if it recovers", "Buy more since it's cheaper now"),
        listOf("Capital preservation (avoid losing money)", "Steady, slow growth", "Maximum profit, regardless of risk"),
        listOf("Less than 1 year", "1 to 3 years", "More than 3 years")
    )

    fun answerQuestion(questionIndex: Int, answerIndex: Int) {
        val currentAnswers = _answers.value.toMutableList()
        if (questionIndex < currentAnswers.size) {
            currentAnswers[questionIndex] = answerIndex
        } else {
            currentAnswers.add(answerIndex)
        }
        _answers.value = currentAnswers
    }

    fun submitAnswers() {
        viewModelScope.launch {
            val profile = calculateRiskProfile(_answers.value)
            userPreferences.saveRiskScore(profile.score)
            _isComplete.value = true
        }
    }
}
