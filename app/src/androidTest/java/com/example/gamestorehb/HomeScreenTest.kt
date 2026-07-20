package com.example.gamestorehb

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.ui.MainActivity
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
