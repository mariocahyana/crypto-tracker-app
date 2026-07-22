package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Sentiment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzeSentimentUseCaseTest {

    private lateinit var analyzeSentimentUseCase: AnalyzeSentimentUseCase

    @Before
    fun setup() {
        analyzeSentimentUseCase = AnalyzeSentimentUseCase()
    }

    @Test
    fun `analyze bullish text should return BULLISH`() = runTest {
        val title = "Bitcoin will surge"
        val body = "The market is entering a massive bull run and hitting all-time high."
        
        // Pass empty url to trigger fallbackBody logic
        val result = analyzeSentimentUseCase("", title, body)
        
        assertEquals(Sentiment.BULLISH, result)
    }

    @Test
    fun `analyze bearish text should return BEARISH`() = runTest {
        val title = "Market crash incoming"
        val body = "Major exchange hacked and facing lawsuit. Price plunge expected."
        
        val result = analyzeSentimentUseCase("", title, body)
        
        assertEquals(Sentiment.BEARISH, result)
    }

    @Test
    fun `analyze negated bearish text should return BULLISH`() = runTest {
        val title = "Bitcoin is safe"
        val body = "Experts confirm that the market will not crash. A scam was prevented."
        
        val result = analyzeSentimentUseCase("", title, body)
        
        // 'not crash' adds to bullish score instead of bearish
        assertEquals(Sentiment.BULLISH, result)
    }

    @Test
    fun `analyze negated bullish text should return BEARISH`() = runTest {
        val title = "Bitcoin struggles"
        val body = "The price did not surge. We won't see an all-time high anytime soon."
        
        val result = analyzeSentimentUseCase("", title, body)
        
        // 'did not surge', 'won't see all-time high' adds to bearish score
        assertEquals(Sentiment.BEARISH, result)
    }

    @Test
    fun `analyze mixed text with low margin should return NEUTRAL`() = runTest {
        val title = "Crypto update"
        // 1 bull point (buy), 1 bear point (sell). Difference is 0, so neutral.
        val body = "Some people want to buy, but others want to sell."
        
        val result = analyzeSentimentUseCase("", title, body)
        
        assertEquals(Sentiment.NEUTRAL, result)
    }
}
