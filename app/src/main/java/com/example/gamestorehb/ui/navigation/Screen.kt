package com.example.gamestorehb.ui.navigation

/**
 * Sealed hierarchy of all navigation destinations in the app.
 * Each object/class defines its [route] string used by the NavHost.
 */
sealed class Screen(val route: String) {
    /** Market overview — entry point of the app. */
    data object Home : Screen("home")

    /** Coin detail screen. Requires a {coinId} nav argument. */
    data object Detail : Screen("detail/{${NavArgs.COIN_ID}}") {
        fun createRoute(coinId: String) = "detail/$coinId"
    }

    /** User's saved portfolio screen. */
    data object Portfolio : Screen("portfolio")

    /** User's risk profile screen. */
    data object RiskProfile : Screen("risk_profile")

    /** Paper Trading dashboard. */
    data object Trading : Screen("trading")

    /** Market News & Sentiment Analysis. */
    data object News : Screen("news")
}

/** Typed nav argument key constants. */
object NavArgs {
    const val COIN_ID = "coinId"
}
