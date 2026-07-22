package com.example.gamestorehb.ui.auth

import com.example.gamestorehb.domain.model.AuthResult
import com.example.gamestorehb.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSuccessAuthRepository : AuthRepository {
        override suspend fun register(username: String, password: String): AuthResult {
            return AuthResult.Success(username)
        }

        override suspend fun login(username: String, password: String): AuthResult {
            return AuthResult.Success(username)
        }

        override suspend fun logout() {}
        
        override fun getLoggedInUsername(): Flow<String> = flowOf("")
        override fun isLoggedIn(): Flow<Boolean> = flowOf(false)
    }

    private class FakeErrorAuthRepository : AuthRepository {
        override suspend fun register(username: String, password: String): AuthResult {
            return AuthResult.Error("Registrasi gagal")
        }

        override suspend fun login(username: String, password: String): AuthResult {
            return AuthResult.Error("Username atau password salah")
        }

        override suspend fun logout() {}
        
        override fun getLoggedInUsername(): Flow<String> = flowOf("")
        override fun isLoggedIn(): Flow<Boolean> = flowOf(false)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with empty username should show username error`() = runTest {
        val viewModel = AuthViewModel(FakeSuccessAuthRepository())
        viewModel.login("", "123456")
        
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Username tidak boleh kosong", state.errorMessage)
    }

    @Test
    fun `login with empty password should show password error`() = runTest {
        val viewModel = AuthViewModel(FakeSuccessAuthRepository())
        viewModel.login("admin", "")
        
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Password tidak boleh kosong", state.errorMessage)
    }

    @Test
    fun `login with password less than 6 chars should show length error`() = runTest {
        val viewModel = AuthViewModel(FakeSuccessAuthRepository())
        viewModel.login("admin", "12345") // 5 characters
        
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Password minimal 6 karakter", state.errorMessage)
    }

    @Test
    fun `login with valid credential should show success`() = runTest {
        val viewModel = AuthViewModel(FakeSuccessAuthRepository())
        viewModel.login("admin", "123456")
        
        testDispatcher.scheduler.advanceUntilIdle() // Wait for coroutine
        
        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("admin", state.successUsername)
        assertNull(state.errorMessage)
    }

    @Test
    fun `login with invalid credential should show error`() = runTest {
        val viewModel = AuthViewModel(FakeErrorAuthRepository())
        viewModel.login("admin", "wrongpass")
        
        testDispatcher.scheduler.advanceUntilIdle() // Wait for coroutine
        
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Username atau password salah", state.errorMessage)
    }

    @Test
    fun `register with unmatched password should show error`() = runTest {
        val viewModel = AuthViewModel(FakeSuccessAuthRepository())
        viewModel.register("admin", "123456", "654321")
        
        val state = viewModel.uiState.value
        assertFalse(state.isSuccess)
        assertEquals("Password tidak cocok", state.errorMessage)
    }
}
