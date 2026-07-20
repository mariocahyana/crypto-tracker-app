package com.example.gamestorehb.ui.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetPortfolioUseCase
import com.example.gamestorehb.domain.usecase.RemoveCoinUseCase
import com.example.gamestorehb.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the state for the Trading Dashboard screen.
 * @param positions Coins the user actually owns (holdings > 0).
 * @param virtualBalance Available virtual cash.
 * @param totalPositionsValue Total USD value of all positions.
 * @param totalUnrealizedPnL Total USD profit/loss across all positions.
 * @param totalCost Total cost basis of all positions.
 */
data class TradingViewState(
    val positions: List<Coin>,
    val virtualBalance: Double,
    val totalPositionsValue: Double,
    val totalUnrealizedPnL: Double,
    val totalCost: Double
) {
    val totalPortfolioValue: Double get() = virtualBalance + totalPositionsValue
    val pnlPercent: Double get() = if (totalCost > 0) (totalUnrealizedPnL / totalCost) * 100 else 0.0
}

@HiltViewModel
class TradingViewModel @Inject constructor(
    getPortfolioUseCase: GetPortfolioUseCase,
    private val removeCoinUseCase: RemoveCoinUseCase,
    userPreferences: UserPreferences
) : ViewModel() {

    val tradingState: StateFlow<UiState<TradingViewState>> = combine(
        getPortfolioUseCase(),
        userPreferences.virtualBalance
    ) { allCoins, virtualBalance ->
        // Only show coins where the user has actually bought some
        val positions = allCoins.filter { it.holdings > 0.000001 }

        val totalPositionsValue = positions.sumOf { it.holdings * it.currentPrice }
        val totalCost = positions.sumOf { it.holdings * it.averageBuyPrice }
        val totalPnL = totalPositionsValue - totalCost

        UiState.Success(
            TradingViewState(
                positions = positions,
                virtualBalance = virtualBalance,
                totalPositionsValue = totalPositionsValue,
                totalUnrealizedPnL = totalPnL,
                totalCost = totalCost
            )
        ) as UiState<TradingViewState>
    }.catch { error ->
        emit(UiState.Error(error.message ?: "Failed to load trading data."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    /** Remove coin from portfolio (also clears position). */
    fun closePosition(coinId: String) {
        viewModelScope.launch {
            removeCoinUseCase(coinId)
        }
    }
}
