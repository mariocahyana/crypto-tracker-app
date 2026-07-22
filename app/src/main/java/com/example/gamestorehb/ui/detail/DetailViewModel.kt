package com.example.gamestorehb.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetCoinDetailUseCase
import com.example.gamestorehb.domain.usecase.GetCoinHistoryUseCase
import com.example.gamestorehb.domain.usecase.RemoveCoinUseCase
import com.example.gamestorehb.domain.usecase.SaveCoinUseCase
import com.example.gamestorehb.ui.navigation.NavArgs
import com.example.gamestorehb.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.usecase.TradeCoinUseCase
import com.example.gamestorehb.domain.usecase.TradeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class DetailViewState(
    val coin: Coin,
    val historyPrices: List<Double> = emptyList()
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCoinDetailUseCase: GetCoinDetailUseCase,
    private val getCoinHistoryUseCase: GetCoinHistoryUseCase,
    private val saveCoinUseCase: SaveCoinUseCase,
    private val removeCoinUseCase: RemoveCoinUseCase,
    private val tradeCoinUseCase: TradeCoinUseCase,
    userPreferences: UserPreferences
) : ViewModel() {

    private val coinId: String = checkNotNull(savedStateHandle[NavArgs.COIN_ID])

    private val _detailState = MutableStateFlow<UiState<DetailViewState>>(UiState.Loading)
    val detailState: StateFlow<UiState<DetailViewState>> = _detailState.asStateFlow()

    private val _bookmarkMessage = MutableStateFlow<String?>(null)
    val bookmarkMessage: StateFlow<String?> = _bookmarkMessage.asStateFlow()

    val virtualBalance: StateFlow<Double> = userPreferences.virtualBalance.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 10_000.0  // Match UserPreferences default so UI shows correct balance immediately
    )

    init {
        loadCoinDetail()
    }

    fun loadCoinDetail() {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            
            // Fetch concurrently
            val coinDeferred = async { getCoinDetailUseCase(coinId) }
            val historyDeferred = async { getCoinHistoryUseCase(coinId, 30) }
            
            val coinResult = coinDeferred.await()
            val historyResult = historyDeferred.await()
            
            coinResult.fold(
                onSuccess = { coin ->
                    val prices = historyResult.getOrNull()?.map { it.second } ?: emptyList()
                    _detailState.value = UiState.Success(DetailViewState(coin, prices))
                },
                onFailure = { error ->
                    _detailState.value = UiState.Error(
                        error.message ?: "Failed to load coin details."
                    )
                }
            )
        }
    }

    fun tradeCoin(tradeType: TradeType, amountUsd: Double) {
        val currentState = _detailState.value
        if (currentState !is UiState.Success) return

        viewModelScope.launch {
            val viewState = currentState.data
            val coin = viewState.coin
            
            val result = tradeCoinUseCase(coin, tradeType, amountUsd)
            result.fold(
                onSuccess = {
                    val action = if (tradeType == TradeType.BUY) "Bought" else "Sold"
                    _bookmarkMessage.value = "Successfully $action $$amountUsd of ${coin.symbol}"
                    // Reload the coin to update holdings and average price locally
                    loadCoinDetail()
                },
                onFailure = { error ->
                    _bookmarkMessage.value = error.message ?: "Trade failed"
                }
            )
        }
    }

    /**
     * Toggles bookmark state for the current coin.
     *
     * If already bookmarked:
     *   - Has holdings → keep Room record (protect trading data), just remove bookmark flag
     *   - No holdings  → delete from Room entirely
     * If not bookmarked:
     *   - Save to Room with isBookmarked = true
     */
    fun toggleBookmark() {
        val currentState = _detailState.value
        if (currentState !is UiState.Success) return

        viewModelScope.launch {
            val viewState = currentState.data
            val coin = viewState.coin
            if (coin.isBookmarked) {
                if (coin.holdings > 0.0) {
                    // Bug 5 fix: coin has active holdings — don't delete from Room!
                    // Just clear the bookmark flag by saving with isBookmarked = false.
                    saveCoinUseCase(coin.copy(isBookmarked = false))
                    _detailState.value = UiState.Success(viewState.copy(coin = coin.copy(isBookmarked = false)))
                    _bookmarkMessage.value = "${coin.name} removed from watchlist (position kept)"
                } else {
                    // No holdings — safe to delete entirely
                    removeCoinUseCase(coin.id)
                    _detailState.value = UiState.Success(viewState.copy(coin = coin.copy(isBookmarked = false)))
                    _bookmarkMessage.value = "${coin.name} removed from portfolio"
                }
            } else {
                saveCoinUseCase(coin)
                _detailState.value = UiState.Success(viewState.copy(coin = coin.copy(isBookmarked = true)))
                _bookmarkMessage.value = "${coin.name} added to portfolio"
            }
        }
    }

    fun clearBookmarkMessage() {
        _bookmarkMessage.value = null
    }
}
