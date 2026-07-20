package com.example.gamestorehb.domain.model

data class NewsArticle(
    val id: String,
    val publishedOn: Long, // timestamp
    val imageUrl: String,
    val title: String,
    val url: String,
    val body: String,
    val sourceName: String,
    val sourceImageUrl: String,
    val sentiment: Sentiment = Sentiment.NEUTRAL
)

enum class Sentiment {
    BULLISH,
    BEARISH,
    NEUTRAL
}
