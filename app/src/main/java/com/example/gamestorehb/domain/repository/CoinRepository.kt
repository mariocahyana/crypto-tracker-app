package com.example.gamestorehb.domain.repository

import com.example.gamestorehb.domain.model.Coin
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer repository interface (the contract).
 * The data layer provides the concrete implementation injected via Hilt.
 * Clean Architecture: the domain layer owns this interface; the data layer depends on it.
 */
interface CoinRepository {

    /** Fetch live market data from the remote API (CoinGecko). */
    suspend fun getCoins(): Result<List<Coin>>

    /** Fetch details for a single coin by ID. */
    suspend fun getCoinById(coinId: String): Result<Coin>
    suspend fun getCoinHistory(id: String, days: Int): Result<List<Pair<Long, Double>>>

    /** Observe the local portfolio (Room DB) as a reactive Flow. */
    fun getPortfolioCoins(): Flow<List<Coin>>

    /** Save a coin to the local portfolio. */
    suspend fun saveCoin(coin: Coin)

    /** Remove a coin from the local portfolio by its ID. */
    suspend fun removeCoin(coinId: String)

    /** Check whether a specific coin is already bookmarked. */
    suspend fun isCoinBookmarked(coinId: String): Boolean

    /** Update holdings and average buy price (Paper Trading) */
    suspend fun updateCoinHoldings(coinId: String, holdings: Double, averagePrice: Double)
}
