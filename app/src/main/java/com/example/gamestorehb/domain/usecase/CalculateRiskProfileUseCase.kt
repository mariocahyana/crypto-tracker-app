package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.RiskProfile
import javax.inject.Inject

class   CalculateRiskProfileUseCase @Inject constructor() {
    
    /**
     * Calculates the user's risk score based on an array of answers.
     * Answers are expected to be indices (0 to 3) representing choices 
     * from most conservative (0) to most aggressive (3).
     */
    operator fun invoke(answers: List<Int>): RiskProfile {
        if (answers.isEmpty()) return RiskProfile.fromScore(5) // Default to moderate

        // Each question has 3 options (index 0, 1, 2). Max index is 2.
        val maxPossibleScore = answers.size * 2
        val actualScore = answers.sum()
        
        // Scale to 1-10
        val normalized = ((actualScore.toFloat() / maxPossibleScore) * 9) + 1
        return RiskProfile.fromScore(normalized.toInt())
    }
}
