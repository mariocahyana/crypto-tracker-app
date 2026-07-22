package com.example.gamestorehb.data.di

import android.content.Context
import androidx.room.Room
import com.example.gamestorehb.data.local.dao.CoinDao
import com.example.gamestorehb.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides Room Database and DAO instances.
 * Scoped to [SingletonComponent] so a single DB instance lives for the app lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideCoinDao(database: AppDatabase): CoinDao = database.coinDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): com.example.gamestorehb.data.local.dao.UserDao = database.userDao()
}

