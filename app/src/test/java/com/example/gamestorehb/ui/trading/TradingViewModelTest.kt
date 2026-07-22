package com.example.gamestorehb.ui.trading

import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetPortfolioUseCase
import com.example.gamestorehb.domain.usecase.RemoveCoinUseCase
import com.example.gamestorehb.util.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TradingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPortfolioUseCase: GetPortfolioUseCase
    private lateinit var removeCoinUseCase: RemoveCoinUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: TradingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPortfolioUseCase = mockk()
        removeCoinUseCase = mockk()
        userPreferences = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `trading state should calculate pnl correctly`() = runTest {
        val mockCoins = listOf(
            Coin(id = "1", name = "Bitcoin", symbol = "BTC", currentPrice = 60000.0, priceChangePercentage24h = 0.0, holdings = 2.0, averageBuyPrice = 50000.0, imageUrl = "", marketCap = 0L, marketCapRank = 1, totalVolume = 0.0, high24h = 0.0, low24h = 0.0, circulatingSupply = 0.0),
            Coin(id = "2", name = "Ethereum", symbol = "ETH", currentPrice = 3000.0, priceChangePercentage24h = 0.0, holdings = 10.0, averageBuyPrice = 4000.0, imageUrl = "", marketCap = 0L, marketCapRank = 2, totalVolume = 0.0, high24h = 0.0, low24h = 0.0, circulatingSupply = 0.0),
            Coin(id = "3", name = "Dogecoin", symbol = "DOGE", currentPrice = 0.5, priceChangePercentage24h = 0.0, holdings = 0.0, averageBuyPrice = 0.0, imageUrl = "", marketCap = 0L, marketCapRank = 3, totalVolume = 0.0, high24h = 0.0, low24h = 0.0, circulatingSupply = 0.0) // Zero holding
        )
        every { getPortfolioUseCase() } returns flowOf(mockCoins)
        every { userPreferences.virtualBalance } returns flowOf(15000.0)

        viewModel = TradingViewModel(getPortfolioUseCase, removeCoinUseCase, userPreferences)
        
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.tradingState.collect()
        }
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.tradingState.value
        assertTrue(state is UiState.Success)
        
        val data = (state as UiState.Success).data
        
        // 2 coins with holdings > 0
        assertEquals(2, data.positions.size)
        
        // Total Position Value:
        // BTC: 2 * 60,000 = 120,000
        // ETH: 10 * 3,000 = 30,000
        // Total = 150,000
        assertEquals(150000.0, data.totalPositionsValue, 0.0)

        // Total Cost:
        // BTC: 2 * 50,000 = 100,000
        // ETH: 10 * 4,000 = 40,000
        // Total = 140,000
        assertEquals(140000.0, data.totalCost, 0.0)

        // Total PnL = 150,000 - 140,000 = 10,000
        assertEquals(10000.0, data.totalUnrealizedPnL, 0.0)
        
        // Portfolio Value = 150,000 + Virtual Balance (15,000) = 165,000
        assertEquals(165000.0, data.totalPortfolioValue, 0.0)
    }

    @Test
    fun `closePosition should call usecase`() = runTest {
        every { getPortfolioUseCase() } returns flowOf(emptyList())
        every { userPreferences.virtualBalance } returns flowOf(10000.0)
        coEvery { removeCoinUseCase(any()) } returns Unit

        viewModel = TradingViewModel(getPortfolioUseCase, removeCoinUseCase, userPreferences)
        
        viewModel.closePosition("bitcoin")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { removeCoinUseCase("bitcoin") }
    }
}
