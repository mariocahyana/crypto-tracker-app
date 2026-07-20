package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Observes the user's saved portfolio from Room DB as a reactive Flow.
 * Drives the PortfolioScreen with live updates whenever the local DB changes.
 */
class GetPortfolioUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    operator fun invoke(): Flow<List<Coin>> = repository.getPortfolioCoins()
}
