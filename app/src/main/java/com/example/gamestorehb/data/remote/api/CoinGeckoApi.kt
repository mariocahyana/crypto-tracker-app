package com.example.gamestorehb.data.remote.api

import com.example.gamestorehb.data.remote.dto.CoinDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the CoinGecko public API (v3).
 * Base URL: https://api.coingecko.com/api/v3/
 *
 * CoinGecko free tier allows ~30 calls/minute without an API key.
 */
interface CoinGeckoApi {

    /**
     * Fetches a paginated list of coins with market data.
     * @param vsCurrency The target currency (default: "usd")
     * @param order Sort order (default: "market_cap_desc")
     * @param perPage Number of results per page (max: 250)
     * @param page Page number (1-indexed)
     * @param sparkline Whether to include 7-day sparkline data
     */
    @GET("coins/markets")
    suspend fun getCoins(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinDto>

    /**
     * Fetches detailed information about a single coin by its ID.
     * @param coinId The CoinGecko coin ID (e.g. "bitcoin", "ethereum")
     * @param localization Whether to include localization data
     * @param tickers Whether to include ticker data
     * @param marketData Whether to include market data
     * @param communityData Whether to include community data
     * @param developerData Whether to include developer data
     */
    @GET("coins/{id}")
    suspend fun getCoinById(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): CoinDetailDto

    /**
     * Fetches historical market data including price, market cap, and volume.
     * @param coinId The CoinGecko coin ID
     * @param vsCurrency Target currency (e.g. "usd")
     * @param days Data up to number of days ago
     * @param interval Data interval (e.g. "daily")
     */
    @GET("coins/{id}/market_chart")
    suspend fun getCoinMarketChart(
        @Path("id") coinId: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: Int = 30,
        @Query("interval") interval: String = "daily"
    ): com.example.gamestorehb.data.remote.dto.HistoryDto
}
