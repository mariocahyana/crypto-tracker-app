package com.example.gamestorehb.data.mapper

import com.example.gamestorehb.data.local.entity.CoinEntity
import com.example.gamestorehb.data.remote.api.CoinDetailDto
import com.example.gamestorehb.data.remote.dto.CoinDto
import com.example.gamestorehb.domain.model.Coin

// ─── CoinDto (Remote) → Domain Coin ───────────────────────────────────────────

/**
 * Maps a CoinGecko market list DTO to the clean domain [Coin] model.
 */
fun CoinDto.toDomain(): Coin = Coin(
    id = id,
    symbol = symbol.uppercase(),
    name = name,
    imageUrl = image,
    currentPrice = currentPrice ?: 0.0,
    marketCap = marketCap ?: 0L,
    marketCapRank = marketCapRank ?: 0,
    priceChangePercentage24h = priceChangePercentage24h ?: 0.0,
    totalVolume = totalVolume ?: 0.0,
    high24h = high24h ?: 0.0,
    low24h = low24h ?: 0.0,
    circulatingSupply = circulatingSupply ?: 0.0,
    isBookmarked = false
)

// ─── CoinDetailDto (Remote) → Domain Coin ─────────────────────────────────────

/**
 * Maps a CoinGecko coin-detail DTO to the clean domain [Coin] model.
 */
fun CoinDetailDto.toDomain(): Coin = Coin(
    id = id,
    symbol = symbol.uppercase(),
    name = name,
    imageUrl = image?.large ?: image?.small ?: image?.thumb ?: "",
    currentPrice = marketData?.currentPrice?.get("usd") ?: 0.0,
    marketCap = marketData?.marketCap?.get("usd") ?: 0L,
    marketCapRank = marketCapRank ?: 0,
    priceChangePercentage24h = marketData?.priceChangePercentage24h ?: 0.0,
    totalVolume = marketData?.totalVolume?.get("usd") ?: 0.0,
    high24h = marketData?.high24h?.get("usd") ?: 0.0,
    low24h = marketData?.low24h?.get("usd") ?: 0.0,
    circulatingSupply = marketData?.circulatingSupply ?: 0.0,
    isBookmarked = false
)

// ─── CoinEntity (Local) → Domain Coin ─────────────────────────────────────────

/**
 * Maps a Room [CoinEntity] to the clean domain [Coin] model.
 * isBookmarked is always true since it's persisted in the portfolio DB.
 */
fun CoinEntity.toDomain(): Coin = Coin(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercentage24h = priceChangePercentage24h,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    isBookmarked = isBookmarked,
    holdings = holdings,
    averageBuyPrice = averageBuyPrice
)

// ─── Domain Coin → CoinEntity (Local) ─────────────────────────────────────────

/**
 * Maps a domain [Coin] to a Room [CoinEntity] for local persistence.
 */
fun Coin.toEntity(): CoinEntity = CoinEntity(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercentage24h = priceChangePercentage24h,
    totalVolume = totalVolume,
    high24h = high24h,
    low24h = low24h,
    circulatingSupply = circulatingSupply,
    holdings = holdings,
    averageBuyPrice = averageBuyPrice,
    isBookmarked = isBookmarked
)
