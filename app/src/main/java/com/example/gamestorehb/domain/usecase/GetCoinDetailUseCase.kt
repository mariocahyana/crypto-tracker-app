package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * Use case: Fetches detailed data for a single coin by its ID.
 * Prepares data for the DetailScreen (risk metrics, price history placeholder).
 */
class GetCoinDetailUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(coinId: String): Result<Coin> = repository.getCoinById(coinId)
}
