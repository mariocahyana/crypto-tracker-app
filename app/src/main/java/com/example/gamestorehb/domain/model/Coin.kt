package com.example.gamestorehb.domain.model

/**
 * Clean Domain Model for a Cryptocurrency asset.
 * This is the single source of truth passed between domain → UI layers.
 * It is decoupled from both Room entities and Retrofit DTOs.
 *
 * Future ML/Behavioral Finance fields (e.g., beta, sharpe ratio) will be added here.
 */
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: Double,
    val marketCap: Long,
    val marketCapRank: Int,
    val priceChangePercentage24h: Double,
    val totalVolume: Double,
    val high24h: Double,
    val low24h: Double,
    val circulatingSupply: Double,
    val isBookmarked: Boolean = false,
    val holdings: Double = 0.0,
    val averageBuyPrice: Double = 0.0
)
