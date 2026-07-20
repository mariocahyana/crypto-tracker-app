package com.example.gamestorehb.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.model.RiskProfile
import com.example.gamestorehb.domain.usecase.GetPortfolioUseCase
import com.example.gamestorehb.domain.usecase.OptimizePortfolioUseCase
import com.example.gamestorehb.domain.usecase.RemoveCoinUseCase
import com.example.gamestorehb.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioViewState(
    val allocations: Map<Coin, Double>,
    val riskProfile: RiskProfile
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    getPortfolioUseCase: GetPortfolioUseCase,
    private val removeCoinUseCase: RemoveCoinUseCase,
    private val optimizePortfolio: OptimizePortfolioUseCase,
    userPreferences: UserPreferences
) : ViewModel() {

    private val riskScoreFlow = userPreferences.riskScore.map { score ->
        score?.let { RiskProfile.fromScore(it) } ?: RiskProfile.fromScore(5)
    }

    val portfolioState: StateFlow<UiState<PortfolioViewState>> = combine(
        getPortfolioUseCase(),
        riskScoreFlow
    ) { coins, riskProfile ->
        if (coins.isEmpty()) {
            UiState.Empty
        } else {
            val weights = optimizePortfolio(coins, riskProfile)
            val coinWeights = coins.associateWith { coin -> weights[coin.id] ?: 0.0 }
            UiState.Success(PortfolioViewState(coinWeights, riskProfile))
        }
    }.catch { error ->
        emit(UiState.Error(error.message ?: "Failed to load portfolio."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun removeFromPortfolio(coinId: String) {
        viewModelScope.launch {
            removeCoinUseCase(coinId)
        }
    }
}
