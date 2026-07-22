package com.example.gamestorehb.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.ui.auth.AuthViewModel
import com.example.gamestorehb.ui.auth.LoginScreen
import com.example.gamestorehb.ui.auth.RegisterScreen
import com.example.gamestorehb.ui.detail.DetailScreen
import com.example.gamestorehb.ui.home.HomeScreen
import com.example.gamestorehb.ui.portfolio.PortfolioScreen
import com.example.gamestorehb.ui.trading.TradingScreen
import com.example.gamestorehb.ui.news.NewsScreen
import com.example.gamestorehb.ui.theme.*
import javax.inject.Inject

/**
 * Root navigation composable — sets up the NavHost with bottom navigation.
 * The Detail screen is outside the bottom nav bar (full-screen push navigation).
 */
@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Observe login state from DataStore
    val loggedInUserId by userPreferences.loggedInUserId.collectAsState(initial = -1)
    val startDestination = if (loggedInUserId != -1) Screen.Home.route else Screen.Login.route

    // Routes that show the bottom nav bar
    val bottomNavRoutes = listOf(Screen.Home.route, Screen.Portfolio.route, Screen.Trading.route, Screen.News.route, Screen.RiskProfile.route)
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 6 },
                            animationSpec = tween(200)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150)) +
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 6 },
                            animationSpec = tween(150)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) +
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 6 },
                            animationSpec = tween(200)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(150)) +
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 6 },
                            animationSpec = tween(150)
                        )
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCoinClick = { coinId ->
                        navController.navigate(Screen.Detail.createRoute(coinId))
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument(NavArgs.COIN_ID) { type = androidx.navigation.NavType.StringType }
                )
            ) {
                DetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Portfolio.route) {
                PortfolioScreen(
                    onNavigateToDetail = { coinId ->
                        navController.navigate(Screen.Detail.createRoute(coinId))
                    }
                )
            }

            composable(Screen.Trading.route) {
                TradingScreen(
                    onNavigateToDetail = { coinId ->
                        navController.navigate(Screen.Detail.createRoute(coinId))
                    }
                )
            }

            composable(Screen.News.route) {
                NewsScreen()
            }

            composable(Screen.RiskProfile.route) {
                com.example.gamestorehb.ui.risk.RiskProfileScreen(
                    onComplete = {
                        navController.navigate(Screen.Portfolio.route) {
                            popUpTo(Screen.RiskProfile.route) { inclusive = true }
                        }
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── Auth Screens (no bottom bar) ──────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // After register, go back to login so user can sign in
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Markets", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Portfolio, "Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    BottomNavItem(Screen.Trading, "Trading", Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    BottomNavItem(Screen.News, "News", Icons.Filled.Article, Icons.Outlined.Article),
    BottomNavItem(Screen.RiskProfile, "Risk Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
private fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = White,
                    selectedTextColor = White,
                    indicatorColor = SurfaceVariant,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                )
            )
        }
    }
}
