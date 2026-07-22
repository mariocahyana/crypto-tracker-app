package com.example.gamestorehb.ui.risk

import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.RiskProfile
import com.example.gamestorehb.domain.usecase.CalculateRiskProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RiskProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var calculateRiskProfile: CalculateRiskProfileUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: RiskProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        calculateRiskProfile = mockk()
        userPreferences = mockk()
        viewModel = RiskProfileViewModel(calculateRiskProfile, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `answerQuestion updates answers list`() {
        viewModel.answerQuestion(0, 1) // Question 0, Answer 1
        
        val answers = viewModel.answers.value
        assertEquals(1, answers.size)
        assertEquals(1, answers[0])
        
        viewModel.answerQuestion(1, 2) // Question 1, Answer 2
        assertEquals(2, viewModel.answers.value.size)
        assertEquals(2, viewModel.answers.value[1])
    }

    @Test
    fun `submitAnswers saves score and updates isComplete`() = runTest {
        val answers = listOf(1, 2, 2)
        viewModel.answerQuestion(0, 1)
        viewModel.answerQuestion(1, 2)
        viewModel.answerQuestion(2, 2)

        val expectedProfile = com.example.gamestorehb.domain.model.RiskProfile(5, com.example.gamestorehb.domain.model.RiskTolerance.MODERATE)
        every { calculateRiskProfile(answers) } returns expectedProfile
        coEvery { userPreferences.saveRiskScore(5) } returns Unit

        viewModel.submitAnswers()
        
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { userPreferences.saveRiskScore(5) }
        assertTrue(viewModel.isComplete.value)
    }
}
