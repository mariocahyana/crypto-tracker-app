package com.example.gamestorehb.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.usecase.GetCoinsUseCase
import com.example.gamestorehb.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [HomeScreen].
 * Fetches the live crypto market list and exposes it as a [StateFlow<UiState>].
 *
 * Search state is also managed here so it survives recompositions.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCoinsUseCase: GetCoinsUseCase
) : ViewModel() {

    // ── Market List State ──────────────────────────────────────────────────────
    private val _coinsState = MutableStateFlow<UiState<List<Coin>>>(UiState.Loading)
    val coinsState: StateFlow<UiState<List<Coin>>> = _coinsState.asStateFlow()

    // ── Search Query State ─────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ── All fetched coins (backing for search filter) ──────────────────────────
    private var allCoins: List<Coin> = emptyList()

    init {
        loadCoins()
    }

    /** Triggers a fresh fetch from the API. */
    fun loadCoins() {
        viewModelScope.launch {
            _coinsState.value = UiState.Loading
            val result = getCoinsUseCase()
            result.fold(
                onSuccess = { coins ->
                    allCoins = coins
                    val filtered = filterCoins(coins, _searchQuery.value)
                    _coinsState.value = if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
                },
                onFailure = { error ->
                    _coinsState.value = UiState.Error(
                        error.message ?: "Failed to load market data. Check your connection."
                    )
                }
            )
        }
    }

    /** Updates search query and filters the cached list in-memory (no new API call). */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        val filtered = filterCoins(allCoins, query)
        _coinsState.value = when {
            allCoins.isEmpty() -> UiState.Loading
            filtered.isEmpty() -> UiState.Empty
            else -> UiState.Success(filtered)
        }
    }

    private fun filterCoins(coins: List<Coin>, query: String): List<Coin> {
        if (query.isBlank()) return coins
        return coins.filter { coin ->
            coin.name.contains(query, ignoreCase = true) ||
                    coin.symbol.contains(query, ignoreCase = true)
        }
    }
}
