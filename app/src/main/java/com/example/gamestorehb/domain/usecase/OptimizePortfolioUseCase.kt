package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.model.RiskProfile
import com.example.gamestorehb.domain.model.RiskTolerance
import javax.inject.Inject

class OptimizePortfolioUseCase @Inject constructor() {

    /**
     * Calculates recommended weight allocations for a list of coins based on the user's Risk Profile.
     * 
     * In a full Markowitz model, this would compute the covariance matrix of historical returns.
     * For this application, we use a simplified heuristic based on market cap rank and volatility
     * (represented by priceChangePercentage24h as a proxy for risk).
     * 
     * Returns a map of Coin ID to Weight Percentage (0.0 to 100.0).
     */
    operator fun invoke(coins: List<Coin>, riskProfile: RiskProfile?): Map<String, Double> {
        if (coins.isEmpty()) return emptyMap()
        if (coins.size == 1) return mapOf(coins.first().id to 100.0)

        val defaultRisk = riskProfile ?: RiskProfile.fromScore(5)
        
        // Simple heuristic: 
        // Bitcoin/Ethereum (Rank 1-2) are considered "Low Risk / Base assets".
        // Altcoins (Rank > 2) are considered "High Risk".
        
        // Calculate a base score for each coin based on risk profile
        val scores = coins.map { coin ->
            val isBaseAsset = coin.symbol.lowercase() in listOf("btc", "eth")
            
            val score = when (defaultRisk.tolerance) {
                RiskTolerance.CONSERVATIVE -> {
                    if (isBaseAsset) 80.0 else 20.0 / coins.size
                }
                RiskTolerance.MODERATE -> {
                    if (isBaseAsset) 50.0 else 50.0 / coins.size
                }
                RiskTolerance.AGGRESSIVE -> {
                    if (isBaseAsset) 20.0 else 80.0 / coins.size
                }
            }
            coin.id to score
        }.toMap()

        // Normalize scores so they sum to 100%
        val totalScore = scores.values.sum()
        return scores.mapValues { (_, score) -> (score / totalScore) * 100.0 }
    }
}
