package com.example.gamestorehb.domain.model

enum class RiskTolerance {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}

data class RiskProfile(
    val score: Int, // 1 to 10
    val tolerance: RiskTolerance
) {
    companion object {
        fun fromScore(score: Int): RiskProfile {
            val tolerance = when {
                score <= 3 -> RiskTolerance.CONSERVATIVE
                score <= 7 -> RiskTolerance.MODERATE
                else -> RiskTolerance.AGGRESSIVE
            }
            return RiskProfile(score, tolerance)
        }
    }
}
