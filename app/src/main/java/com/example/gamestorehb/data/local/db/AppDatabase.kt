package com.example.gamestorehb.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gamestorehb.data.local.dao.CoinDao
import com.example.gamestorehb.data.local.dao.UserDao
import com.example.gamestorehb.data.local.entity.CoinEntity
import com.example.gamestorehb.data.local.entity.UserEntity

/**
 * Room Database — the main local persistence layer.
 *
 * Version history:
 *  v1 → v2: (initial portfolio schema)
 *  v2 → v3: Added [UserEntity] table for local authentication
 *
 * Using fallbackToDestructiveMigration in DatabaseModule so we
 * don't need to write explicit migrations during development.
 */
@Database(
    entities = [CoinEntity::class, UserEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "crypto_portfolio_db"
    }
}
