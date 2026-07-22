package com.example.gamestorehb

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso / Compose UI Tests for the Markets (Home) screen.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @org.junit.Before
    fun setupLogin() {
        try {
            // Wait briefly for initial compose to settle
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
            composeTestRule.waitForIdle()

            // If login screen is visible, do login flow
            val isLoginScreen = composeTestRule
                .onAllNodesWithTag("usernameField")
                .fetchSemanticsNodes().isNotEmpty()

            if (isLoginScreen) {
                // Navigate to Register
                composeTestRule.onNodeWithText("Daftar").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                // Fill register form
                composeTestRule.onNodeWithText("Username").performTextInput("testuser99")
                composeTestRule.onNodeWithText("Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Konfirmasi Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Daftar Sekarang").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                // Login
                composeTestRule.onNodeWithTag("usernameField").performTextInput("testuser99")
                composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
                composeTestRule.onNodeWithTag("loginButton").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            // Already logged in or handled
        }
    }

    // ── 1. Search bar is displayed ────────────────────────────────────────────

    @Test
    fun homeScreen_searchBar_isVisible() {
        composeTestRule.onNodeWithTag("search_field").assertIsDisplayed()
    }

    // ── 2. Typing in search filters the list ─────────────────────────────────

    @Test
    fun homeScreen_search_filtersList() {
        // Wait for coins to load (up to 10 seconds)
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("coin_list_item").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag("search_field")
            .performTextInput("bitcoin")

        // At least one result should still be visible
        composeTestRule
            .onAllNodesWithTag("coin_list_item")
            .onFirst()
            .assertIsDisplayed()
    }

    // ── 3. Coin list has items after loading ─────────────────────────────────

    @Test
    fun homeScreen_afterLoading_coinListNotEmpty() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("coin_list_item").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("coin_list_item")[0].assertIsDisplayed()
    }
}
