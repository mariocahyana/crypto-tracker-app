package com.example.gamestorehb.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for the CoinGecko /coins/{id}/market_chart endpoint.
 * Contains arrays of [timestamp, price] pairs.
 */
data class HistoryDto(
    @SerializedName("prices")
    val prices: List<List<Double>>
)
