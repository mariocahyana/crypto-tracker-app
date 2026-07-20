package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * Use case: Saves a coin to the local Room database (user's portfolio).
 */
class SaveCoinUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(coin: Coin) = repository.saveCoin(coin)
}
