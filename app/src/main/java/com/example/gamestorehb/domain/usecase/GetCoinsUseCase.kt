package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import javax.inject.Inject

/**
 * Use case: Fetches the full list of crypto coins from the remote market API.
 * Part of Clean Architecture's domain interactor layer.
 */
class GetCoinsUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    suspend operator fun invoke(): Result<List<Coin>> = repository.getCoins()
}
