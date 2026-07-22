package com.example.gamestorehb.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a registered user account.
 * Password is stored as a SHA-256 hash (never plain text).
 * Username has a UNIQUE index to prevent duplicate registrations.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val passwordHash: String,   // SHA-256(salt + password)
    val salt: String,           // Random hex string, unique per user
    val createdAt: Long = System.currentTimeMillis()
)
