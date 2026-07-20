package com.example.gamestorehb.data.remote.api

import com.google.gson.annotations.SerializedName

/**
 * Detailed DTO for a single coin fetched from GET /coins/{id}.
 * Nested market data is used to populate the DetailScreen risk metrics section.
 */
data class CoinDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: ImageDto?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("market_data") val marketData: MarketDataDto?
)

data class ImageDto(
    @SerializedName("thumb") val thumb: String?,
    @SerializedName("small") val small: String?,
    @SerializedName("large") val large: String?
)

data class MarketDataDto(
    @SerializedName("current_price") val currentPrice: Map<String, Double>?,
    @SerializedName("market_cap") val marketCap: Map<String, Long>?,
    @SerializedName("total_volume") val totalVolume: Map<String, Double>?,
    @SerializedName("high_24h") val high24h: Map<String, Double>?,
    @SerializedName("low_24h") val low24h: Map<String, Double>?,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerializedName("circulating_supply") val circulatingSupply: Double?,
    @SerializedName("ath") val ath: Map<String, Double>?,
    @SerializedName("atl") val atl: Map<String, Double>?
)
