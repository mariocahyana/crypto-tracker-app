package com.example.gamestorehb.data.repository

import com.example.gamestorehb.data.local.dao.CoinDao
import com.example.gamestorehb.data.mapper.toDomain
import com.example.gamestorehb.data.mapper.toEntity
import com.example.gamestorehb.data.remote.api.CoinGeckoApi
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Concrete implementation of [CoinRepository].
 * Acts as the single source of truth — mediates between [CoinGeckoApi] (remote)
 * and [CoinDao] (local Room DB).
 *
 * Remote failures are caught and wrapped in [Result.failure] to propagate
 * cleanly to the ViewModels without crashing the app.
 */
class CoinRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: CoinDao
) : CoinRepository {

    /**
     * Fetches live market data from CoinGecko.
     * Retries once with a 3-second delay if a 429 Too Many Requests is received.
     */
    override suspend fun getCoins(): Result<List<Coin>> {
        return fetchWithRetry { api.getCoins().map { it.toDomain() } }
    }

    /**
     * Fetches detailed data for a single coin.
     * Also checks the local DB to set the [Coin.isBookmarked] flag correctly.
     */
    override suspend fun getCoinById(coinId: String): Result<Coin> {
        return fetchWithRetry {
            val dto = api.getCoinById(coinId)
            val localCoin = dao.getCoinById(coinId) // Fetch local data (holdings, avg price)
            
            val coin = dto.toDomain().copy(
                isBookmarked = localCoin?.isBookmarked ?: false,
                holdings = localCoin?.holdings ?: 0.0,
                averageBuyPrice = localCoin?.averageBuyPrice ?: 0.0
            )
            listOf(coin)
        }.map { it.first() }
    }

    /**
     * Retries [block] once on HTTP 429 after a 3-second back-off.
     * Wraps all outcomes in [Result].
     */
    private suspend fun <T> fetchWithRetry(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            if (e.code() == 429) {
                delay(3_000)
                try { Result.success(block()) } catch (retryEx: Exception) {
                    Result.failure(Exception("Rate limited (HTTP 429). Please try again in a moment."))
                }
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches historical price data.
     * Maps the List<List<Double>> from the API to List<Pair<Timestamp, Price>>.
     */
    override suspend fun getCoinHistory(id: String, days: Int): Result<List<Pair<Long, Double>>> {
        return try {
            val response = api.getCoinMarketChart(coinId = id, days = days)
            // The API returns [timestamp, price] pairs as inner lists.
            val history = response.prices.map { point ->
                Pair(point[0].toLong(), point[1])
            }
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a reactive [Flow] of the user's portfolio from Room.
     * Auto-emits whenever the DB changes.
     */
    override fun getPortfolioCoins(): Flow<List<Coin>> {
        return dao.getAllCoins().map { entities -> entities.map { it.toDomain() } }
    }

    /** Persists a coin to the portfolio Room database. */
    override suspend fun saveCoin(coin: Coin) {
        dao.insertCoin(coin.toEntity())
    }

    /** Deletes a coin from the portfolio by ID. */
    override suspend fun removeCoin(coinId: String) {
        dao.deleteCoinById(coinId)
    }

    /** Returns true if the coin is in the local portfolio. */
    override suspend fun isCoinBookmarked(coinId: String): Boolean {
        return dao.isCoinBookmarked(coinId) > 0
    }

    /** Updates the coin holdings and average buy price. */
    override suspend fun updateCoinHoldings(coinId: String, holdings: Double, averagePrice: Double) {
        dao.updateCoinHoldings(coinId, holdings, averagePrice)
    }
}
