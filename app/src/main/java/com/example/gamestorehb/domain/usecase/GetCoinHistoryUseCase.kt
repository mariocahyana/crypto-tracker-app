package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.repository.CoinRepository
import javax.inject.Inject

class GetCoinHistoryUseCase @Inject constructor(
    private val repository: CoinRepository
) {
    /**
     * Fetches historical price data for a coin.
     * @param coinId The ID of the coin
     * @param days Number of days (default 30)
     * @return Result containing a list of timestamp and price pairs
     */
    suspend operator fun invoke(coinId: String, days: Int = 30): Result<List<Pair<Long, Double>>> {
        return repository.getCoinHistory(coinId, days)
    }
}
