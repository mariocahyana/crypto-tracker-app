package com.example.gamestorehb.domain.model

/** Result of a login or register operation. */
sealed class AuthResult {
    data class Success(val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
