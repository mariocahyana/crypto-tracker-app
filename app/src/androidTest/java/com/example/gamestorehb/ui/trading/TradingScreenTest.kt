package com.example.gamestorehb.ui.trading

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetPortfolioUseCase
import com.example.gamestorehb.ui.theme.CryptoPortfolioTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.content.Context
import androidx.test.core.app.ApplicationProvider

@RunWith(AndroidJUnit4::class)
class TradingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class DummyGetPortfolioUseCase : GetPortfolioUseCase(
        object : com.example.gamestorehb.domain.repository.CoinRepository {
            override suspend fun getCoins(): Result<List<Coin>> = Result.success(emptyList())
            override suspend fun getCoinById(coinId: String): Result<Coin> = Result.success(
                Coin("1", "BTC", "Bitcoin", "", 0.0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )
            override suspend fun getCoinHistory(id: String, days: Int): Result<List<Pair<Long, Double>>> = Result.success(emptyList())
            override fun getPortfolioCoins(): kotlinx.coroutines.flow.Flow<List<Coin>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun saveCoin(coin: Coin) {}
            override suspend fun removeCoin(coinId: String) {}
            override suspend fun isCoinBookmarked(coinId: String): Boolean = false
            override suspend fun updateCoinHoldings(coinId: String, holdings: Double, averagePrice: Double) {}
        }
    ) {
        override operator fun invoke(): kotlinx.coroutines.flow.Flow<List<Coin>> {
            return flowOf(
                listOf(
                    Coin(
                        id = "1",
                        name = "Bitcoin",
                        symbol = "BTC",
                        currentPrice = 1000.0,
                        priceChangePercentage24h = 5.0,
                        holdings = 2.0,
                        averageBuyPrice = 800.0,
                        imageUrl = "",
                        marketCap = 0L,
                        marketCapRank = 1,
                        totalVolume = 0.0,
                        high24h = 0.0,
                        low24h = 0.0,
                        circulatingSupply = 0.0
                    )
                )
            )
        }
    }
    
    private class DummyRemoveCoinUseCase : com.example.gamestorehb.domain.usecase.RemoveCoinUseCase(
        object : com.example.gamestorehb.domain.repository.CoinRepository {
            override suspend fun getCoins(): Result<List<Coin>> = Result.success(emptyList())
            override suspend fun getCoinById(coinId: String): Result<Coin> = Result.success(
                Coin("1", "BTC", "Bitcoin", "", 0.0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )
            override suspend fun getCoinHistory(id: String, days: Int): Result<List<Pair<Long, Double>>> = Result.success(emptyList())
            override fun getPortfolioCoins(): kotlinx.coroutines.flow.Flow<List<Coin>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun saveCoin(coin: Coin) {}
            override suspend fun removeCoin(coinId: String) {}
            override suspend fun isCoinBookmarked(coinId: String): Boolean = false
            override suspend fun updateCoinHoldings(coinId: String, holdings: Double, averagePrice: Double) {}
        }
    ) {
        override suspend operator fun invoke(coinId: String) {}
    }

    @Test
    fun tradingScreen_displaysHeroBalanceAndPositions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userPrefs = UserPreferences(context)
        
        composeTestRule.setContent {
            CryptoPortfolioTheme {
                TradingScreen(
                    onNavigateToDetail = {},
                    viewModel = TradingViewModel(
                        DummyGetPortfolioUseCase(),
                        DummyRemoveCoinUseCase(),
                        userPrefs
                    )
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Trading").assertExists()
        composeTestRule.onNodeWithTag("hero_balance").assertExists()
        
        // Assert that at least one metric is present
        composeTestRule.onNodeWithText("Invested").assertExists()
        
        // Assert that Bitcoin is shown in the active positions list
        composeTestRule.onNodeWithText("Bitcoin").assertExists()
    }
}
