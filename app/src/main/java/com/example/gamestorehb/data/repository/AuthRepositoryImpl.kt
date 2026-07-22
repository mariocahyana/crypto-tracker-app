package com.example.gamestorehb.data.repository

import com.example.gamestorehb.data.local.dao.UserDao
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.data.local.entity.UserEntity
import com.example.gamestorehb.domain.model.AuthResult
import com.example.gamestorehb.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

/**
 * Implements local authentication using Room + DataStore.
 *
 * Security:
 * - Passwords are hashed with SHA-256 and a random per-user salt.
 * - Plain text passwords are NEVER persisted.
 */
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun register(username: String, password: String): AuthResult {
        return try {
            // Validation
            if (username.length < 3) return AuthResult.Error("Username minimal 3 karakter")
            if (password.length < 6) return AuthResult.Error("Password minimal 6 karakter")
            if (userDao.isUsernameTaken(username) > 0) {
                return AuthResult.Error("Username '$username' sudah digunakan")
            }

            // Hash password with random salt
            val salt = generateSalt()
            val hash = hashPassword(password, salt)

            userDao.insertUser(
                UserEntity(username = username, passwordHash = hash, salt = salt)
            )
            AuthResult.Success(username)
        } catch (e: Exception) {
            AuthResult.Error("Registrasi gagal: ${e.message}")
        }
    }

    override suspend fun login(username: String, password: String): AuthResult {
        return try {
            val user = userDao.getUserByUsername(username)
                ?: return AuthResult.Error("Username tidak ditemukan")

            val hash = hashPassword(password, user.salt)
            if (hash != user.passwordHash) {
                return AuthResult.Error("Password salah")
            }

            userPreferences.saveSession(userId = user.id, username = user.username)
            AuthResult.Success(user.username)
        } catch (e: Exception) {
            AuthResult.Error("Login gagal: ${e.message}")
        }
    }

    override suspend fun logout() {
        userPreferences.clearSession()
    }

    override fun getLoggedInUsername(): Flow<String> = userPreferences.loggedInUsername

    override fun isLoggedIn(): Flow<Boolean> = userPreferences.loggedInUserId.map { it != -1 }

    // ── Crypto helpers ─────────────────────────────────────────────────────────

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    private fun hashPassword(password: String, salt: String): String {
        val input = (salt + password).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input).toHex()
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
