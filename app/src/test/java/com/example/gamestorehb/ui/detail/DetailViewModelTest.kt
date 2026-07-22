package com.example.gamestorehb.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetCoinDetailUseCase
import com.example.gamestorehb.domain.usecase.GetCoinHistoryUseCase
import com.example.gamestorehb.domain.usecase.RemoveCoinUseCase
import com.example.gamestorehb.domain.usecase.SaveCoinUseCase
import com.example.gamestorehb.domain.usecase.TradeCoinUseCase
import com.example.gamestorehb.ui.navigation.NavArgs
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
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCoinDetailUseCase: GetCoinDetailUseCase
    private lateinit var getCoinHistoryUseCase: GetCoinHistoryUseCase
    private lateinit var saveCoinUseCase: SaveCoinUseCase
    private lateinit var removeCoinUseCase: RemoveCoinUseCase
    private lateinit var tradeCoinUseCase: TradeCoinUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: DetailViewModel

    /** Bookmarked Bitcoin with active holdings */
    private val bookmarkedBtcWithHoldings = Coin(
        id = "bitcoin", name = "Bitcoin", symbol = "BTC",
        currentPrice = 50_000.0, priceChangePercentage24h = 2.0,
        holdings = 1.5, averageBuyPrice = 40_000.0,
        imageUrl = "", marketCap = 0L, marketCapRank = 1,
        totalVolume = 0.0, high24h = 0.0, low24h = 0.0, circulatingSupply = 0.0,
        isBookmarked = true
    )

    /** Bookmarked Bitcoin with NO holdings (pure watchlist) */
    private val bookmarkedBtcNoHoldings = bookmarkedBtcWithHoldings.copy(
        holdings = 0.0, averageBuyPrice = 0.0
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCoinDetailUseCase = mockk()
        getCoinHistoryUseCase = mockk()
        saveCoinUseCase = mockk(relaxed = true)
        removeCoinUseCase = mockk(relaxed = true)
        tradeCoinUseCase = mockk()
        userPreferences = mockk()

        every { userPreferences.virtualBalance } returns flowOf(10_000.0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(coin: Coin): DetailViewModel {
        coEvery { getCoinDetailUseCase(any()) } returns Result.success(coin)
        coEvery { getCoinHistoryUseCase(any(), any()) } returns Result.success(emptyList())

        val savedState = SavedStateHandle(mapOf(NavArgs.COIN_ID to "bitcoin"))
        return DetailViewModel(
            savedStateHandle = savedState,
            getCoinDetailUseCase = getCoinDetailUseCase,
            getCoinHistoryUseCase = getCoinHistoryUseCase,
            saveCoinUseCase = saveCoinUseCase,
            removeCoinUseCase = removeCoinUseCase,
            tradeCoinUseCase = tradeCoinUseCase,
            userPreferences = userPreferences
        )
    }

    // ── toggleBookmark Tests ──────────────────────────────────────────────────

    /**
     * Bug 5 fix: Un-bookmarking a coin that has active holdings MUST NOT delete
     * from Room. It should save with isBookmarked = false to preserve trading data.
     */
    @Test
    fun `un-bookmark coin with holdings saves with isBookmarked=false, does NOT delete`() = runTest {
        viewModel = buildViewModel(bookmarkedBtcWithHoldings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleBookmark()
        testDispatcher.scheduler.advanceUntilIdle()

        // Must NOT delete (would lose trading data)
        coVerify(exactly = 0) { removeCoinUseCase(any()) }

        // Must save with isBookmarked = false
        coVerify(exactly = 1) { saveCoinUseCase(bookmarkedBtcWithHoldings.copy(isBookmarked = false)) }

        // Local state should reflect isBookmarked = false
        val state = viewModel.detailState.value
        assertTrue(state is UiState.Success)
        assertFalse((state as UiState.Success).data.coin.isBookmarked)
    }

    @Test
    fun `un-bookmark coin with NO holdings deletes from Room`() = runTest {
        viewModel = buildViewModel(bookmarkedBtcNoHoldings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleBookmark()
        testDispatcher.scheduler.advanceUntilIdle()

        // No holdings → safe to delete
        coVerify(exactly = 1) { removeCoinUseCase("bitcoin") }
        coVerify(exactly = 0) { saveCoinUseCase(any()) }

        val state = viewModel.detailState.value
        assertTrue(state is UiState.Success)
        assertFalse((state as UiState.Success).data.coin.isBookmarked)
    }

    @Test
    fun `add bookmark calls saveCoinUseCase and updates state`() = runTest {
        val unbookmarkedCoin = bookmarkedBtcWithHoldings.copy(isBookmarked = false)
        viewModel = buildViewModel(unbookmarkedCoin)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleBookmark()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveCoinUseCase(unbookmarkedCoin) }
        coVerify(exactly = 0) { removeCoinUseCase(any()) }

        val state = viewModel.detailState.value
        assertTrue((state as UiState.Success).data.coin.isBookmarked)
    }

    @Test
    fun `bookmark message is set after toggle`() = runTest {
        viewModel = buildViewModel(bookmarkedBtcNoHoldings)
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially null
        assertNull(viewModel.bookmarkMessage.value)

        viewModel.toggleBookmark()
        testDispatcher.scheduler.advanceUntilIdle()

        // Message should be set (non-null)
        assertNotNull(viewModel.bookmarkMessage.value)
    }

    @Test
    fun `clearBookmarkMessage sets message to null`() = runTest {
        viewModel = buildViewModel(bookmarkedBtcNoHoldings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleBookmark()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.bookmarkMessage.value)

        viewModel.clearBookmarkMessage()
        assertNull(viewModel.bookmarkMessage.value)
    }
}
