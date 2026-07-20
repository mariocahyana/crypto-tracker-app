package com.example.gamestorehb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a bookmarked cryptocurrency in the user's portfolio.
 * Stored in the "coins" table in the local Room database.
 */
@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey val id: String,
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
    /** Timestamp of when the user bookmarked this asset. */
    val bookmarkedAt: Long = System.currentTimeMillis(),
    /** Amount of coin owned by the user (Paper Trading) */
    val holdings: Double = 0.0,
    /** Average buy price in USD for calculating PnL (Paper Trading) */
    val averageBuyPrice: Double = 0.0
)
