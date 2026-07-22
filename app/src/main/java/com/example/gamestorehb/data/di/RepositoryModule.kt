package com.example.gamestorehb.data.di

import com.example.gamestorehb.data.repository.CoinRepositoryImpl
import com.example.gamestorehb.domain.repository.CoinRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds the [CoinRepositoryImpl] concrete class
 * to the [CoinRepository] interface.
 * This enables Dependency Inversion — use cases depend on the abstraction,
 * not the implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoinRepository(
        coinRepositoryImpl: CoinRepositoryImpl
    ): CoinRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: com.example.gamestorehb.data.repository.NewsRepositoryImpl
    ): com.example.gamestorehb.domain.repository.NewsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.example.gamestorehb.data.repository.AuthRepositoryImpl
    ): com.example.gamestorehb.domain.repository.AuthRepository
}

