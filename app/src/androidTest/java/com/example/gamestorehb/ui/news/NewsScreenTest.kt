package com.example.gamestorehb.ui.news

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.model.Sentiment
import com.example.gamestorehb.domain.usecase.GetNewsUseCase
import com.example.gamestorehb.ui.theme.CryptoPortfolioTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()



    // A simpler way without MockK in Instrumented Test is providing a Fake ViewModel,
    // but since ViewModel creates it in init, we can just supply FakeGetNewsUseCase.
    // Wait, mockk() won't work easily in Instrumented test without setup.
    // Let's create a manual mock.
    
    private class DummyGetNewsUseCase : GetNewsUseCase(
        repository = object : com.example.gamestorehb.domain.repository.NewsRepository {
            override fun getNews(): kotlinx.coroutines.flow.Flow<List<NewsArticle>> = kotlinx.coroutines.flow.flowOf(emptyList())
        },
        analyzeSentiment = com.example.gamestorehb.domain.usecase.AnalyzeSentimentUseCase()
    ) {
        override operator fun invoke(): kotlinx.coroutines.flow.Flow<List<NewsArticle>> {
            return flowOf(
                listOf(
                    NewsArticle(
                        id = "1",
                        title = "Bitcoin hits 100k",
                        body = "It is amazing",
                        sourceName = "CryptoNews",
                        url = "",
                        publishedOn = System.currentTimeMillis() / 1000,
                        imageUrl = "",
                        sourceImageUrl = "",
                        sentiment = Sentiment.BULLISH
                    )
                )
            )
        }
    }

    @Test
    fun newsScreen_shouldDisplayArticlesAndSentiment() {
        composeTestRule.setContent {
            CryptoPortfolioTheme {
                NewsScreen(
                    viewModel = NewsViewModel(DummyGetNewsUseCase())
                )
            }
        }

        // Wait for UI to settle
        composeTestRule.waitForIdle()

        // Assert Title is present
        composeTestRule.onNodeWithText("Market News").assertExists()

        // Assert List and Item is present
        composeTestRule.onNodeWithTag("news_list").assertExists()
        composeTestRule.onAllNodesWithTag("news_item").onFirst().assertExists()

        // Assert Sentiment Badge
        composeTestRule.onAllNodesWithTag("sentiment_badge", useUnmergedTree = true).onFirst().assertExists()
        composeTestRule.onNodeWithText("Bullish").assertExists()
    }
}
