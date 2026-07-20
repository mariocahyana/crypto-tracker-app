package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * Use case: Removes a coin from the local portfolio by its CoinGecko ID.
 */
class RemoveCoinUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(coinId: String) = repository.removeCoin(coinId)
}
