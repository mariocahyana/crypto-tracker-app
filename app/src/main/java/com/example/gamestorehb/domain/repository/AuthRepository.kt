package com.example.gamestorehb.domain.repository

import com.example.gamestorehb.domain.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(username: String, password: String): AuthResult
    suspend fun login(username: String, password: String): AuthResult
    suspend fun logout()
    fun getLoggedInUsername(): Flow<String>
    fun isLoggedIn(): Flow<Boolean>
}
