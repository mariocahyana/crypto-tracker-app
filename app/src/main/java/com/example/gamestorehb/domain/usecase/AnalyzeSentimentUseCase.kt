package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Sentiment
import javax.inject.Inject

class AnalyzeSentimentUseCase @Inject constructor() {
    
    // Simple heuristic-based keyword scanning
    private val bullishKeywords = listOf(
        "surge", "bull", "adoption", "launch", "partner", "upgrade",
        "soar", "pump", "record", "high", "gain", "rally", "buy"
    )
    
    private val bearishKeywords = listOf(
        "crash", "hack", "sec", "ban", "scam", "drop", "plunge", 
        "bear", "sell", "low", "loss", "lawsuit", "sue", "regulate"
    )

    operator fun invoke(title: String, body: String): Sentiment {
        val content = "$title $body".lowercase()
        
        var bullScore = 0
        var bearScore = 0

        bullishKeywords.forEach { if (content.contains(it)) bullScore++ }
        bearishKeywords.forEach { if (content.contains(it)) bearScore++ }

        return when {
            bullScore > bearScore -> Sentiment.BULLISH
            bearScore > bullScore -> Sentiment.BEARISH
            else -> Sentiment.NEUTRAL
        }
    }
}
