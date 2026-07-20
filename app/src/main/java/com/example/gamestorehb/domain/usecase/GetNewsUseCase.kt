package com.example.gamestorehb.domain.usecase

import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository,
    private val analyzeSentiment: AnalyzeSentimentUseCase
) {
    operator fun invoke(): Flow<List<NewsArticle>> {
        return repository.getNews().map { articles ->
            articles.map { article ->
                val sentiment = analyzeSentiment(article.title, article.body)
                article.copy(sentiment = sentiment)
            }
        }
    }
}
