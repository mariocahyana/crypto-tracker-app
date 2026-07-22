package com.example.gamestorehb.data.di

import android.content.Context
import com.example.gamestorehb.data.remote.api.CoinGeckoApi
import com.example.gamestorehb.data.repository.CoinRepositoryImpl
import com.example.gamestorehb.domain.repository.CoinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing Retrofit, OkHttpClient, and the API interface.
 * Also binds [CoinRepositoryImpl] to the [CoinRepository] interface.
 *
 * Features:
 * - OkHttp disk cache (10 MB): caches successful CoinGecko responses for 60s.
 *   This prevents HTTP 429 "Too Many Requests" errors when the user refreshes
 *   the Markets screen frequently.
 * - Stale-if-error: on network failure, serves cached data up to 24h old.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.coingecko.com/api/v3/"
    private const val TIMEOUT_SECONDS = 10L
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024 // 10 MB
    private const val CACHE_MAX_AGE_SECONDS = 60           // fresh for 60s
    private const val CACHE_STALE_SECONDS = 86400          // stale-if-error: 24h

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE_BYTES)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // Disabled for performance
        }
    }

    /**
     * Interceptor that rewrites the Cache-Control header on responses from
     * CoinGecko so that OkHttp caches them even though the server doesn't
     * send a cacheable response header.
     */
    @Provides
    @Singleton
    fun provideCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header(
                    "Cache-Control",
                    "public, max-age=$CACHE_MAX_AGE_SECONDS, stale-if-error=$CACHE_STALE_SECONDS"
                )
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        cacheInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(cacheInterceptor)   // applied to network responses
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCoinGeckoApi(retrofit: Retrofit): CoinGeckoApi {
        return retrofit.create(CoinGeckoApi::class.java)
    }
}
