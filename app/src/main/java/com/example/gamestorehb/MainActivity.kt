package com.example.gamestorehb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gamestorehb.data.local.datastore.UserPreferences
import com.example.gamestorehb.ui.navigation.AppNavigation
import com.example.gamestorehb.ui.theme.CryptoPortfolioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single Activity that hosts the entire Compose UI tree.
 * All navigation happens inside [AppNavigation] NavHost.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoPortfolioTheme {
                AppNavigation(userPreferences = userPreferences)
            }
        }
    }
}