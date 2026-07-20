package com.example.gamestorehb.data.remote.api

import com.example.gamestorehb.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v1/api.json")
    suspend fun getNews(
        @Query("rss_url") rssUrl: String = "https://cointelegraph.com/rss"
    ): NewsResponseDto
}
