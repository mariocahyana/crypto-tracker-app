package com.example.gamestorehb.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) that maps directly to the CoinGecko API JSON response.
 * This model is ONLY used in the data layer — it gets mapped to the domain [Coin] model
 * by the repository before being exposed to the domain/UI layers.
 *
 * CoinGecko endpoint: GET /coins/markets?vs_currency=usd
 */
data class CoinDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("current_price")
    val currentPrice: Double?,

    @SerializedName("market_cap")
    val marketCap: Long?,

    @SerializedName("market_cap_rank")
    val marketCapRank: Int?,

    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double?,

    @SerializedName("total_volume")
    val totalVolume: Double?,

    @SerializedName("high_24h")
    val high24h: Double?,

    @SerializedName("low_24h")
    val low24h: Double?,

    @SerializedName("circulating_supply")
    val circulatingSupply: Double?
)
