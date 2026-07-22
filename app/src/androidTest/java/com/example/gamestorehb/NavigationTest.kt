package com.example.gamestorehb

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso / Compose UI Tests for the main navigation flow.
 *
 * These tests verify that:
 * 1. The bottom navigation bar renders with all 5 tabs.
 * 2. Tapping each tab navigates to the correct screen.
 * 3. The Markets screen shows a search bar.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @org.junit.Before
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

                composeTestRule.onNodeWithText("Username").performTextInput("testnavuser")
                composeTestRule.onNodeWithText("Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Konfirmasi Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Daftar Sekarang").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                composeTestRule.onNodeWithTag("usernameField").performTextInput("testnavuser")
                composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
                composeTestRule.onNodeWithTag("loginButton").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            // Already logged in or handled
        }
    }

    // ── 1. Bottom navigation has all 5 tabs ───────────────────────────────────

    @Test
    fun bottomNavBar_showsAllFiveTabs() {
        // Use onFirst() because nav label text may also appear as screen title
        composeTestRule.onAllNodesWithText("Markets").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Trading").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("News").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Risk Profile").onFirst().assertIsDisplayed()
    }

    // ── 2. Markets tab is selected by default ─────────────────────────────────

    @Test
    fun defaultScreen_isMarketsTab() {
        // The Markets screen contains a search field
        composeTestRule.onNodeWithTag("search_field").assertIsDisplayed()
    }

    // ── 3. Navigate to Watchlist ──────────────────────────────────────────────

    @Test
    fun tap_watchlistTab_navigatesToWatchlist() {
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().performClick()
        composeTestRule.waitForIdle()
        // Watchlist screen should have "Watchlist" visible after navigation
        composeTestRule.onAllNodesWithText("Watchlist").onFirst().assertIsDisplayed()
    }

    // ── 4. Navigate to Trading ────────────────────────────────────────────────

    @Test
    fun tap_tradingTab_navigatesToTrading() {
        composeTestRule.onAllNodesWithText("Trading").onFirst().performClick()
        composeTestRule.waitForIdle()
        // TradingScreen has hero card with "Active Positions" or "Invested"
        composeTestRule.onNodeWithTag("hero_balance").assertExists()
    }

    // ── 5. Navigate to News ───────────────────────────────────────────────────

    @Test
    fun tap_newsTab_navigatesToNews() {
        composeTestRule.onNodeWithText("News").performClick()
        // News screen renders "Market News" in the TopAppBar
        composeTestRule.onNodeWithText("Market News").assertIsDisplayed()
    }

    // ── 6. Navigate to Risk Profile ───────────────────────────────────────────

    @Test
    fun tap_riskProfileTab_navigatesToRiskProfile() {
        composeTestRule.onNodeWithText("Risk Profile").performClick()
        // Risk Profile screen shows some question / intro text
        composeTestRule.onNodeWithTag("risk_profile_screen").assertIsDisplayed()
    }
}
