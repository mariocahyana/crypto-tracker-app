package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TradeCoinUseCaseTest {

    private lateinit var repository: CoinRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var useCase: TradeCoinUseCase

    /** A fresh Bitcoin with no prior holdings, not bookmarked */
    private val freshBtc = Coin(
        id = "bitcoin", name = "Bitcoin", symbol = "BTC",
        currentPrice = 50_000.0, priceChangePercentage24h = 0.0,
        holdings = 0.0, averageBuyPrice = 0.0,
        imageUrl = "", marketCap = 0L, marketCapRank = 1,
        totalVolume = 0.0, high24h = 0.0, low24h = 0.0, circulatingSupply = 0.0,
        isBookmarked = false
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        userPreferences = mockk()
        useCase = TradeCoinUseCase(repository, userPreferences)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUY Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `buy with zero amount returns failure`() = runTest {
        every { userPreferences.virtualBalance } returns flowOf(10_000.0)

        val result = useCase(freshBtc, TradeType.BUY, 0.0)

        assertTrue(result.isFailure)
        assertEquals("Amount must be greater than 0", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `buy with insufficient balance returns failure`() = runTest {
        every { userPreferences.virtualBalance } returns flowOf(1_000.0)

        val result = useCase(freshBtc, TradeType.BUY, 5_000.0)

        assertTrue(result.isFailure)
        assertEquals("Insufficient virtual balance.", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `buy success reduces balance by exact amount`() = runTest {
        every { userPreferences.virtualBalance } returns flowOf(10_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.saveCoin(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        val result = useCase(freshBtc, TradeType.BUY, 5_000.0)

        assertTrue(result.isSuccess)
        // Balance $10,000 - $5,000 = $5,000
        coVerify { userPreferences.updateVirtualBalance(5_000.0) }
    }

    @Test
    fun `buy success updates holdings and average price correctly`() = runTest {
        every { userPreferences.virtualBalance } returns flowOf(10_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.saveCoin(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        // Buy $5,000 at $50,000 per BTC → 0.1 BTC, avgPrice = $50,000
        useCase(freshBtc, TradeType.BUY, 5_000.0)

        coVerify { repository.updateCoinHoldings("bitcoin", 0.1, 50_000.0) }
    }

    @Test
    fun `buy twice computes correct weighted average buy price`() = runTest {
        // 1st purchase: 1 BTC at $40,000 (cost = $40,000)
        val btcWithHoldings = freshBtc.copy(holdings = 1.0, averageBuyPrice = 40_000.0, isBookmarked = true)
        every { userPreferences.virtualBalance } returns flowOf(20_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        // 2nd purchase: $10,000 at current $50,000 → 0.2 BTC
        useCase(btcWithHoldings, TradeType.BUY, 10_000.0)

        // totalCostBasis = (1 × 40,000) + 10,000 = $50,000
        // newHoldings    = 1 + 0.2 = 1.2 BTC
        // weightedAvg    = $50,000 / 1.2 = $41,666.67
        val expectedAvgPrice = 50_000.0 / 1.2
        coVerify {
            repository.updateCoinHoldings(
                "bitcoin",
                withArg { assertEquals(1.2, it, 0.0001) },
                withArg { assertEquals(expectedAvgPrice, it, 0.01) }
            )
        }
    }

    @Test
    fun `buy non-bookmarked coin saves to Room before updating holdings`() = runTest {
        every { userPreferences.virtualBalance } returns flowOf(10_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.saveCoin(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        // freshBtc.isBookmarked = false → must call saveCoin first
        useCase(freshBtc, TradeType.BUY, 1_000.0)

        coVerify(exactly = 1) { repository.saveCoin(freshBtc) }
    }

    @Test
    fun `buy bookmarked coin does NOT call saveCoin again`() = runTest {
        val bookmarkedBtc = freshBtc.copy(isBookmarked = true)
        every { userPreferences.virtualBalance } returns flowOf(10_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        useCase(bookmarkedBtc, TradeType.BUY, 1_000.0)

        coVerify(exactly = 0) { repository.saveCoin(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SELL Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `sell with zero amount returns failure`() = runTest {
        val btcWithHoldings = freshBtc.copy(holdings = 1.0, averageBuyPrice = 50_000.0)
        every { userPreferences.virtualBalance } returns flowOf(5_000.0)

        val result = useCase(btcWithHoldings, TradeType.SELL, 0.0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `sell more than holdings value returns failure`() = runTest {
        // 0.1 BTC @ $50,000 → value = $5,000
        val btcWithHoldings = freshBtc.copy(holdings = 0.1, averageBuyPrice = 50_000.0)
        every { userPreferences.virtualBalance } returns flowOf(1_000.0)

        // Try to sell $6,000 → exceeds holdings value of $5,000
        val result = useCase(btcWithHoldings, TradeType.SELL, 6_000.0)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Insufficient"))
    }

    @Test
    fun `partial sell increases balance and reduces holdings, avgPrice unchanged`() = runTest {
        // 1 BTC at $50,000 → value = $50,000
        val btcWithHoldings = freshBtc.copy(holdings = 1.0, averageBuyPrice = 50_000.0)
        every { userPreferences.virtualBalance } returns flowOf(5_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        // Sell $10,000 → 0.2 BTC sold at $50,000
        useCase(btcWithHoldings, TradeType.SELL, 10_000.0)

        // Balance: $5,000 + $10,000 = $15,000
        coVerify { userPreferences.updateVirtualBalance(15_000.0) }

        // Holdings: 1.0 - 0.2 = 0.8 BTC; avgPrice stays at $50,000 (sell never changes avgPrice)
        coVerify {
            repository.updateCoinHoldings(
                "bitcoin",
                withArg { assertEquals(0.8, it, 0.0001) },
                withArg { assertEquals(50_000.0, it, 0.01) }
            )
        }
    }

    // ── Bug 3 fix tests: Sell-all cleanup ────────────────────────────────────

    @Test
    fun `sell all non-bookmarked coin removes it from Room entirely`() = runTest {
        // 0.1 BTC, NOT bookmarked → after selling all, should be deleted from Room
        val btcWithHoldings = freshBtc.copy(holdings = 0.1, averageBuyPrice = 50_000.0, isBookmarked = false)
        every { userPreferences.virtualBalance } returns flowOf(5_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.removeCoin(any()) } returns Unit

        val result = useCase(btcWithHoldings, TradeType.SELL, 5_000.0)

        assertTrue(result.isSuccess)
        // Must call removeCoin (not updateCoinHoldings) to avoid ghost records
        coVerify(exactly = 1) { repository.removeCoin("bitcoin") }
        coVerify(exactly = 0) { repository.updateCoinHoldings(any(), any(), any()) }
    }

    @Test
    fun `sell all bookmarked coin keeps record in Room with zero holdings`() = runTest {
        // 0.1 BTC, IS bookmarked → after selling all, keep record with holdings=0
        val btcBookmarked = freshBtc.copy(holdings = 0.1, averageBuyPrice = 50_000.0, isBookmarked = true)
        every { userPreferences.virtualBalance } returns flowOf(5_000.0)
        coEvery { userPreferences.updateVirtualBalance(any()) } returns Unit
        coEvery { repository.updateCoinHoldings(any(), any(), any()) } returns Unit

        val result = useCase(btcBookmarked, TradeType.SELL, 5_000.0)

        assertTrue(result.isSuccess)
        // Must NOT delete (user still wants to watch), just zero out holdings
        coVerify(exactly = 0) { repository.removeCoin(any()) }
        coVerify { repository.updateCoinHoldings("bitcoin", 0.0, 0.0) }
    }
}
