package com.example.gamestorehb.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.domain.model.AuthResult
import com.example.gamestorehb.domain.repository.AuthRepository
import com.example.gamestorehb.ui.theme.CryptoPortfolioTheme
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeAuthRepository(private val shouldSucceed: Boolean) : AuthRepository {
        override suspend fun register(username: String, password: String): AuthResult {
            return AuthResult.Success(username)
        }

        override suspend fun login(username: String, password: String): AuthResult {
            delay(100) // Simulate network delay
            return if (shouldSucceed) {
                AuthResult.Success(username)
            } else {
                AuthResult.Error("Username atau password salah")
            }
        }

        override suspend fun logout() {}
        
        override fun getLoggedInUsername(): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flowOf("")
        override fun isLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    }

    @Test
    fun loginScreen_shouldDisplayAllComponents() {
        composeTestRule.setContent {
            CryptoPortfolioTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToRegister = {},
                    viewModel = AuthViewModel(FakeAuthRepository(true))
                )
            }
        }

        composeTestRule.onNodeWithText("Masuk ke akun Anda").assertExists()
        composeTestRule.onNodeWithTag("usernameField").assertExists()
        composeTestRule.onNodeWithTag("passwordField").assertExists()
        composeTestRule.onNodeWithTag("loginButton").assertExists()
    }

    @Test
    fun usernameField_shouldAcceptInput() {
        composeTestRule.setContent {
            CryptoPortfolioTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToRegister = {},
                    viewModel = AuthViewModel(FakeAuthRepository(true))
                )
            }
        }

        composeTestRule.onNodeWithTag("usernameField").performTextInput("testuser")
        composeTestRule.onNodeWithText("testuser").assertExists()
    }

    @Test
    fun loginScreen_fullFlowFailed_shouldDisplayErrorMessage() {
        composeTestRule.setContent {
            CryptoPortfolioTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToRegister = {},
                    viewModel = AuthViewModel(FakeAuthRepository(false))
                )
            }
        }

        composeTestRule.onNodeWithTag("usernameField").performTextInput("wronguser")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("wrongpass")
        
        // Ensure the button is enabled before clicking
        composeTestRule.onNodeWithTag("loginButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("loginButton").performClick()

        // Wait for the ViewModel to process and update state
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("errorMessage").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("errorMessage").assertExists()
        composeTestRule.onNodeWithText("Username atau password salah").assertExists()
    }

    @Test
    fun loginScreen_fullFlowSuccess_shouldNavigate() {
        var navigated = false

        composeTestRule.setContent {
            CryptoPortfolioTheme {
                LoginScreen(
                    onLoginSuccess = { navigated = true },
                    onNavigateToRegister = {},
                    viewModel = AuthViewModel(FakeAuthRepository(true))
                )
            }
        }

        composeTestRule.onNodeWithTag("usernameField").performTextInput("admin")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
        
        composeTestRule.onNodeWithTag("loginButton").performClick()

        // Wait for login success logic to run
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            navigated
        }

        assert(navigated)
    }
}
