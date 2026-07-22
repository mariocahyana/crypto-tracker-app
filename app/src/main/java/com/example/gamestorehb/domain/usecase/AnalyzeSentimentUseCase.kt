package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.Sentiment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

/**
 * Advanced Sentiment Analyzer (Lexicon-based with Negation Handling & Weighted Scoring).
 * Fetches the actual article HTML, parses text, and evaluates market sentiment.
 */
class AnalyzeSentimentUseCase @Inject constructor() {
    
    // 1. Weighted Dictionaries (Higher score = stronger impact)
    private val bullishKeywords = mapOf(
        "surge" to 2, "bull" to 2, "soar" to 2, "pump" to 3, "rally" to 2, 
        "breakout" to 3, "all-time high" to 3, "ath" to 3, "halving" to 3,
        "adoption" to 1, "launch" to 1, "partner" to 1, "upgrade" to 1, 
        "record" to 1, "high" to 1, "gain" to 1, "buy" to 1, "support" to 1, 
        "approve" to 2, "green" to 1, "momentum" to 1, "accumulation" to 2
    )
    
    private val bearishKeywords = mapOf(
        "crash" to 3, "hack" to 3, "bankrupt" to 3, "scam" to 3, "plunge" to 3, 
        "liquidated" to 2, "fud" to 2, "scandal" to 2, "sec" to 1, "ban" to 2, 
        "drop" to 1, "bear" to 2, "sell" to 1, "low" to 1, "loss" to 1, 
        "lawsuit" to 2, "sue" to 2, "regulate" to 1, "resistance" to 1, 
        "investigate" to 2, "red" to 1, "inflation" to 1, "scare" to 1
    )

    // Words that reverse the meaning of the following keyword
    private val negations = setOf(
        "not", "didn't", "didnt", "won't", "wont", "never", "no", 
        "cannot", "can't", "cant", "doesn't", "doesnt", "hardly", "barely", "prevent", "avoid"
    )

    suspend operator fun invoke(url: String, title: String, fallbackBody: String): Sentiment = withContext(Dispatchers.IO) {
        var fullText = ""
        try {
            // Attempt to fetch the actual news article content using Jsoup
            val doc = org.jsoup.Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get()
            
            // Extract text from all paragraph tags
            val paragraphs = doc.select("p")
            val sb = java.lang.StringBuilder()
            for (p in paragraphs) {
                sb.append(p.text()).append(" ")
            }
            fullText = sb.toString()
        } catch (e: Exception) {
            // Ignore errors (e.g., 403, timeout) and fallback to RSS description
        }

        if (fullText.trim().isEmpty()) {
            fullText = fallbackBody
        }

        // Clean text: lowercase and remove punctuation around words to help matching
        val content = "$title $fullText".lowercase()
        
        var bullScore = 0
        var bearScore = 0

        // 2. Algorithm with Negation Detection
        fun processDictionary(dictionary: Map<String, Int>, isBullishDictionary: Boolean) {
            dictionary.forEach { (keyword, weight) ->
                // Find whole word matches only
                val regex = Regex("\\b$keyword\\b")
                val matches = regex.findAll(content)
                
                for (match in matches) {
                    // Extract text immediately preceding the keyword (look back up to 30 characters)
                    val startIndex = max(0, match.range.first - 30)
                    val precedingText = content.substring(startIndex, match.range.first).trim()
                    
                    // Split by spaces to get the last few words
                    val precedingWords = precedingText.split(Regex("\\s+"))
                    
                    // Check if any of the preceding 3 words is a negation word
                    val isNegated = precedingWords.takeLast(3).any { it in negations }
                    
                    if (isBullishDictionary) {
                        if (isNegated) bearScore += weight else bullScore += weight
                    } else {
                        if (isNegated) bullScore += weight else bearScore += weight
                    }
                }
            }
        }

        processDictionary(bullishKeywords, true)
        processDictionary(bearishKeywords, false)

        // 3. Margin computation to avoid noise (e.g. 1 point difference might just be noise)
        when {
            bullScore > bearScore + 2 -> Sentiment.BULLISH
            bearScore > bullScore + 2 -> Sentiment.BEARISH
            else -> Sentiment.NEUTRAL
        }
    }
}
