package com.example.gamestorehb.ui.trading

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamestorehb.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Detail Screen + Trade Bottom Sheet.
 * Navigates to first coin in list and tests all trade interactions.
 *
 * Note: Each test gets a fresh Activity via @Rule. The @Before navigates to
 * the first coin in the list, and @After presses back to ensure clean teardown.
 */
@RunWith(AndroidJUnit4::class)
class DetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setupAndNavigate() {
        try {
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
            composeTestRule.waitForIdle()

            // Login if needed
            val isLoginScreen = composeTestRule
                .onAllNodesWithTag("usernameField")
                .fetchSemanticsNodes().isNotEmpty()

            if (isLoginScreen) {
                composeTestRule.onNodeWithText("Daftar").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                composeTestRule.onNodeWithText("Username").performTextInput("detailuser")
                composeTestRule.onNodeWithText("Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Konfirmasi Password").performTextInput("123456")
                composeTestRule.onNodeWithText("Daftar Sekarang").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)

                composeTestRule.onNodeWithTag("usernameField").performTextInput("detailuser")
                composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")
                composeTestRule.onNodeWithTag("loginButton").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(1500)
            }

            // Wait for coin list then click first coin
            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithTag("coin_list_item").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithTag("coin_list_item")[0].performClick()
            composeTestRule.waitForIdle()

            // Wait until Trade FAB appears (detail screen fully loaded)
            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithTag("trade_fab").fetchSemanticsNodes().isNotEmpty()
            }

        } catch (e: Exception) {
            // Already on detail screen or network slow — continue
        }
    }

    @After
    fun tearDown() {
        // Close any open ModalBottomSheet / back stack before Activity teardown
        try {
            Espresso.pressBackUnconditionally()
        } catch (e: Exception) {
            // Ignore if nothing to close
        }
    }

    // ── 1. Detail page: Market Stats section is visible ──────────────────────

    @Test
    fun detailScreen_showsMarketStatsSection() {
        composeTestRule.onNodeWithText("Market Stats").assertExists()
    }

    // ── 2. Trade FAB is visible ───────────────────────────────────────────────

    @Test
    fun detailScreen_tradeFab_isVisible() {
        composeTestRule.onNodeWithTag("trade_fab").assertIsDisplayed()
    }

    // ── 3. Tapping Trade opens bottom sheet with Buy/Sell ────────────────────

    @Test
    fun detailScreen_tapTrade_opensBuySellSheet() {
        composeTestRule.onNodeWithTag("trade_fab").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Buy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sell").assertIsDisplayed()
    }

    // ── 4. Switch to Sell tab shows Confirm Sell button ──────────────────────

    @Test
    fun detailScreen_tradeSheet_sellTabShowsConfirmSell() {
        composeTestRule.onNodeWithTag("trade_fab").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sell").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Confirm Sell").assertIsDisplayed()
    }

    // ── 5. Available balance label is shown in trade sheet ───────────────────

    @Test
    fun detailScreen_tradeSheet_showsAvailableLabel() {
        composeTestRule.onNodeWithTag("trade_fab").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Available:", substring = true).assertExists()
    }
}
