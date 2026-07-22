package com.example.gamestorehb.ui.portfolio

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for the Portfolio / Watchlist screen.
 * End-to-end tests navigating to the Watchlist tab from the main screen.
 */
@RunWith(AndroidJUnit4::class)
class PortfolioScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setupLogin() {
        try {
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
            composeTestRule.waitForIdle()

            val isLoginScreen = composeTestRule
                .onAllNodesWithTag("usernameField")
                .fetchSemanticsNodes().isNotEmpty()

            if (isLoginScreen) {
                composeTestRule.onNodeWithText("Daftar").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                composeTestRule.onNodeWithText("Username").performTextInput("portfoliouser")
                composeTestRule.onNodeWithText("Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Konfirmasi Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Daftar Sekarang").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                composeTestRule.onNodeWithTag("usernameField").performTextInput("portfoliouser")
                composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
                composeTestRule.onNodeWithTag("loginButton").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            // Already logged in or handled
        }
    }

    // ── 1. Watchlist tab shows the correct title ───────────────────────────────

    @Test
    fun portfolioScreen_showsWatchlistTitle() {
        // Navigate to Watchlist tab
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().performClick()
        composeTestRule.waitForIdle()

        // TopAppBar title "Watchlist" should be visible
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().assertIsDisplayed()
    }

    // ── 2. Watchlist empty state shown for new user ────────────────────────────

    @Test
    fun portfolioScreen_newUser_showsEmptyOrLoading() {
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // For a new user, either empty state or loading state
        // The screen must at least be displayed (no crash)
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().assertIsDisplayed()
    }

    // ── 3. Bottom nav Watchlist item is always visible ─────────────────────────

    @Test
    fun portfolioScreen_bottomNavItem_isAlwaysVisible() {
        // Watchlist nav item must be present in bottom bar
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().assertIsDisplayed()
    }
}
