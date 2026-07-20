package com.example.gamestorehb.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.ui.components.AllocationBarChart
import com.example.gamestorehb.ui.components.EmptyView
import com.example.gamestorehb.ui.components.ErrorView
import com.example.gamestorehb.ui.components.LoadingIndicator
import com.example.gamestorehb.ui.theme.*
import com.example.gamestorehb.util.UiState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val state by viewModel.portfolioState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Watchlist", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val uiState = state) {
                is UiState.Loading -> LoadingIndicator()
                is UiState.Empty -> {
                    EmptyView(subtitle = "Your watchlist is empty.\nBookmark coins to track them here.")
                }
                is UiState.Error -> {
                    ErrorView(message = uiState.message, onRetry = { })
                }
                is UiState.Success -> {
                    PortfolioContent(
                        viewState = uiState.data,
                        onCoinClick = onNavigateToDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioContent(
    viewState: PortfolioViewState,
    onCoinClick: (String) -> Unit
) {
    val sortedAllocations = viewState.allocations.entries.sortedByDescending { it.value }
    val weights = sortedAllocations.map { it.value }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ─── Investment Style Chip + AI Allocation Card ───────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Style chip
                Row(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = viewState.riskProfile.tolerance.name.lowercase()
                                .replaceFirstChar { it.uppercase() } + " Investor",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }

                // AI Allocation card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🤖  AI Recommended Allocation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        AllocationBarChart(
                            allocations = weights,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        // Legend rows with "Owned" badge
                        sortedAllocations.forEach { (coin, weight) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = coin.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (coin.holdings > 0.000001) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Positive.copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "Owned",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Positive,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = String.format("%.1f%%", weight),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── Tracked Assets header ────────────────────────────────────────────
        item {
            Text(
                text = "Tracked Assets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // ─── Coin rows ────────────────────────────────────────────────────────
        items(sortedAllocations.map { it.key }, key = { it.id }) { coin ->
            WatchlistCoinItem(coin = coin, onClick = { onCoinClick(coin.id) })
            HorizontalDivider(
                color = OutlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun WatchlistCoinItem(coin: Coin, onClick: () -> Unit) {
    val priceFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val isPositive = coin.priceChangePercentage24h >= 0
    val changeColor = if (isPositive) Positive else Negative

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Logo with optional green dot overlay
        Box {
            AsyncImage(
                model = coin.imageUrl,
                contentDescription = "${coin.name} logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
            )
            if (coin.holdings > 0.000001) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Positive)
                )
            }
        }

        // Name + symbol + owned badge
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (coin.holdings > 0.000001) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Positive.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Owned",
                            style = MaterialTheme.typography.labelSmall,
                            color = Positive,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Price + 24h change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = priceFormatter.format(coin.currentPrice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (isPositive) "▲" else "▼"} ${String.format(Locale.US, "%.2f", abs(coin.priceChangePercentage24h))}%",
                style = MaterialTheme.typography.labelSmall,
                color = changeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
