package com.example.gamestorehb.domain.repository

import com.example.gamestorehb.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNews(): Flow<List<NewsArticle>>
}
