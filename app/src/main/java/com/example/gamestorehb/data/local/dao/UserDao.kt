package com.example.gamestorehb.data.local.dao

import androidx.room.*
import com.example.gamestorehb.data.local.entity.UserEntity

/**
 * Room DAO for user authentication operations.
 */
@Dao
interface UserDao {

    /** Insert a new user. Aborts if username already exists (UNIQUE constraint). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    /** Find a user by their username for login verification. */
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    /** Check if a username is already taken. Returns count (0 or 1). */
    @Query("SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username)")
    suspend fun isUsernameTaken(username: String): Int

    /** Fetch user by ID (used after login to refresh session data). */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?
}
