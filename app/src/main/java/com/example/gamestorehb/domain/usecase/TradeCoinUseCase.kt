package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

enum class TradeType {
    BUY, SELL
}

class TradeCoinUseCase @Inject constructor(
    private val repository: CoinRepository,
    private val userPreferences: UserPreferences
) {
    /**
     * Executes a paper trade.
     * @param coin The coin to trade
     * @param tradeType BUY or SELL
     * @param amountUsd The amount in USD to trade
     * @return Result indicating success or failure reason
     */
    suspend operator fun invoke(coin: Coin, tradeType: TradeType, amountUsd: Double): Result<Unit> {
        if (amountUsd <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))

        val currentBalance = userPreferences.virtualBalance.first()
        val amountCrypto = amountUsd / coin.currentPrice

        return when (tradeType) {
            TradeType.BUY -> {
                if (currentBalance < amountUsd) {
                    return Result.failure(Exception("Insufficient virtual balance."))
                }

                val newBalance = currentBalance - amountUsd
                
                // Calculate new average buy price
                val totalCostBasis = (coin.holdings * coin.averageBuyPrice) + amountUsd
                val newHoldings = coin.holdings + amountCrypto
                val newAvgPrice = if (newHoldings > 0) totalCostBasis / newHoldings else 0.0

                // Update DataStore and Room
                userPreferences.updateVirtualBalance(newBalance)
                
                // Make sure it's saved in Room first if not bookmarked
                if (!coin.isBookmarked) {
                    repository.saveCoin(coin)
                }
                repository.updateCoinHoldings(coin.id, newHoldings, newAvgPrice)
                Result.success(Unit)
            }
            TradeType.SELL -> {
                // Using a small epsilon for floating point comparison issues
                val currentHoldingsValue = coin.holdings * coin.currentPrice
                if (currentHoldingsValue + 0.01 < amountUsd) {
                    return Result.failure(Exception("Insufficient coin holdings to sell this amount."))
                }

                val newBalance = currentBalance + amountUsd
                val newHoldings = maxOf(0.0, coin.holdings - amountCrypto)

                userPreferences.updateVirtualBalance(newBalance)

                if (newHoldings <= 0.000001) {
                    // Sold all holdings
                    if (coin.isBookmarked) {
                        // Keep record in Room (user wants to watch it), just clear holdings
                        repository.updateCoinHoldings(coin.id, 0.0, 0.0)
                    } else {
                        // Not bookmarked — remove ghost record from Room entirely
                        repository.removeCoin(coin.id)
                    }
                } else {
                    // Partial sell — average buy price does not change on sell
                    repository.updateCoinHoldings(coin.id, newHoldings, coin.averageBuyPrice)
                }
                Result.success(Unit)
            }
        }
    }
}
