package com.example.gamestorehb.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gamestorehb.data.local.dao.CoinDao
import com.example.gamestorehb.data.local.entity.CoinEntity

/**
 * Room Database — the main local persistence layer for the portfolio.
 *
 * Version: 1
 * Entities: [CoinEntity]
 *
 * Increment version and provide a Migration when the schema changes.
 */
@Database(
    entities = [CoinEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao

    companion object {
        const val DATABASE_NAME = "crypto_portfolio_db"
    }
}
