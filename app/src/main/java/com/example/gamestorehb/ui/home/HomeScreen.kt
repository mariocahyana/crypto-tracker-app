package com.example.gamestorehb.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamestorehb.ui.components.CoinListItem
import com.example.gamestorehb.ui.components.EmptyView
import com.example.gamestorehb.ui.components.ErrorView
import com.example.gamestorehb.ui.components.LoadingIndicator
import com.example.gamestorehb.ui.theme.*
import com.example.gamestorehb.util.UiState

/**
 * Home Screen — displays live crypto market data as a searchable LazyColumn.
 *
 * UI States handled:
 * - [UiState.Loading]  → [LoadingIndicator]
 * - [UiState.Success]  → [LazyColumn] of [CoinListItem]
 * - [UiState.Empty]    → [EmptyView] (search no results)
 * - [UiState.Error]    → [ErrorView] with retry
 */
@Composable
fun HomeScreen(
    onCoinClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val coinsState by viewModel.coinsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        HomeHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange
        )

        HorizontalDivider(color = Outline, thickness = 0.5.dp)

        // ── Content ────────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = coinsState) {
                is UiState.Loading -> LoadingIndicator(message = "Fetching market data...")

                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Column header labels
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Asset",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                Text(
                                    text = "Price / 24h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }
                        items(
                            items = state.data,
                            key = { coin -> coin.id }
                        ) { coin ->
                            CoinListItem(
                                coin = coin,
                                onClick = { onCoinClick(coin.id) }
                            )
                            HorizontalDivider(
                                color = OutlineVariant,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }

                is UiState.Empty -> EmptyView(
                    title = "No results found",
                    subtitle = "Try a different coin name or symbol."
                )

                is UiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = viewModel::loadCoins
                )
            }
        }
    }
}

// ─── Header Composable ────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Markets",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Text(
            text = "Live crypto market data",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Search coins...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentWhite,
                unfocusedBorderColor = Outline,
                focusedContainerColor = SurfaceVariant,
                unfocusedContainerColor = SurfaceVariant,
                cursorColor = White,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
