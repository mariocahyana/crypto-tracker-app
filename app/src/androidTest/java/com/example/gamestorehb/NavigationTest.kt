package com.example.gamestorehb

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.ui.MainActivity
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

    // ── 1. Bottom navigation has all 5 tabs ───────────────────────────────────

    @Test
    fun bottomNavBar_showsAllFiveTabs() {
        composeTestRule.onNodeWithText("Markets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Watchlist").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trading").assertIsDisplayed()
        composeTestRule.onNodeWithText("News").assertIsDisplayed()
        composeTestRule.onNodeWithText("Risk Profile").assertIsDisplayed()
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
        composeTestRule.onNodeWithText("Watchlist").performClick()
        // Watchlist screen has "Watchlist" as its TopAppBar title
        composeTestRule.onNodeWithText("Watchlist").assertIsDisplayed()
    }

    // ── 4. Navigate to Trading ────────────────────────────────────────────────

    @Test
    fun tap_tradingTab_navigatesToTrading() {
        composeTestRule.onNodeWithText("Trading").performClick()
        // Trading screen renders a hero card with "Total Value" text
        composeTestRule.onNodeWithText("Total Value").assertIsDisplayed()
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
