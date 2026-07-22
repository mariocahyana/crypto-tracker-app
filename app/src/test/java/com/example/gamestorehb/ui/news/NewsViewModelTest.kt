package com.example.gamestorehb.ui.news

import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.model.Sentiment
import com.example.gamestorehb.domain.usecase.GetNewsUseCase
import com.example.gamestorehb.util.UiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class NewsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getNewsUseCase: GetNewsUseCase
    private lateinit var viewModel: NewsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getNewsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchNews returns success state when articles are fetched`() = runTest {
        val mockArticles = listOf(
            NewsArticle("1", System.currentTimeMillis() / 1000, "imageUrl", "Title 1", "Url 1", "Body 1", "Source 1", "sourceImageUrl", Sentiment.BULLISH)
        )
        every { getNewsUseCase() } returns flowOf(mockArticles)

        viewModel = NewsViewModel(getNewsUseCase)
        
        // Initial state should be loading
        assertTrue(viewModel.newsState.value is UiState.Loading)
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.newsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(Sentiment.BULLISH, state.data[0].sentiment)
    }

    @Test
    fun `fetchNews returns empty state when list is empty`() = runTest {
        every { getNewsUseCase() } returns flowOf(emptyList())

        viewModel = NewsViewModel(getNewsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.newsState.value
        assertTrue(state is UiState.Empty)
    }

    @Test
    fun `fetchNews returns error state on exception`() = runTest {
        every { getNewsUseCase() } returns flow { throw Exception("Network Error") }

        viewModel = NewsViewModel(getNewsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.newsState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network Error", (state as UiState.Error).message)
    }
}
