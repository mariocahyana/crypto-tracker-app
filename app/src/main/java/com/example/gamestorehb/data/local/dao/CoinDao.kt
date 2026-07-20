package com.example.gamestorehb.data.local.dao

import androidx.room.*
import com.example.gamestorehb.data.local.entity.CoinEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for portfolio coin operations.
 * Uses Flow for reactive queries so the UI auto-updates when the DB changes.
 */
@Dao
interface CoinDao {

    /**
     * Observe all bookmarked coins, ordered by when they were added (newest first).
     * Returns a [Flow] so Room will automatically push updates.
     */
    @Query("SELECT * FROM coins ORDER BY bookmarkedAt DESC")
    fun getAllCoins(): Flow<List<CoinEntity>>

    /**
     * Fetch a single coin by its CoinGecko ID.
     * Returns null if not found in the local portfolio.
     */
    @Query("SELECT * FROM coins WHERE id = :coinId LIMIT 1")
    suspend fun getCoinById(coinId: String): CoinEntity?

    /**
     * Insert a coin into the portfolio. Replaces any existing entry with the same ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoin(coin: CoinEntity)

    /**
     * Delete a coin from the portfolio by its ID.
     */
    @Query("DELETE FROM coins WHERE id = :coinId")
    suspend fun deleteCoinById(coinId: String)

    /**
     * Check if a coin is already bookmarked. Returns 1 if exists, 0 otherwise.
     */
    @Query("SELECT COUNT(*) FROM coins WHERE id = :coinId")
    suspend fun isCoinBookmarked(coinId: String): Int

    /**
     * Update the holdings and average buy price for a specific coin.
     */
    @Query("UPDATE coins SET holdings = :holdings, averageBuyPrice = :averageBuyPrice WHERE id = :coinId")
    suspend fun updateCoinHoldings(coinId: String, holdings: Double, averageBuyPrice: Double)
}
