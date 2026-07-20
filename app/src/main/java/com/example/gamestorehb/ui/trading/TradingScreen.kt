package com.example.gamestorehb.ui.trading

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.ui.components.LoadingIndicator
import com.example.gamestorehb.ui.theme.*
import com.example.gamestorehb.util.UiState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: TradingViewModel = hiltViewModel()
) {
    val state by viewModel.tradingState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trading",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingIndicator()
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = TextSecondary)
                }
            }
            else -> {
                val data = (s as? UiState.Success)?.data
                TradingContent(
                    viewState = data,
                    modifier = Modifier.padding(padding),
                    onCoinClick = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
private fun TradingContent(
    viewState: TradingViewState?,
    modifier: Modifier = Modifier,
    onCoinClick: (String) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val totalBalance = viewState?.totalPortfolioValue ?: 10_000.0
    val cashBalance = viewState?.virtualBalance ?: 10_000.0
    val pnlUsd = viewState?.totalUnrealizedPnL ?: 0.0
    val pnlPercent = viewState?.pnlPercent ?: 0.0
    val isProfit = pnlUsd >= 0
    val positions = viewState?.positions ?: emptyList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ─── Hero Balance Card ────────────────────────────────────────────────
        item {
            BalanceHeroCard(
                totalBalance = totalBalance,
                cashBalance = cashBalance,
                pnlUsd = pnlUsd,
                pnlPercent = pnlPercent,
                isProfit = isProfit,
                currencyFormatter = currencyFormatter
            )
        }

        // ─── Metrics Row ──────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Invested",
                    value = currencyFormatter.format(viewState?.totalPositionsValue ?: 0.0),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Cash",
                    value = currencyFormatter.format(cashBalance),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Positions",
                    value = "${positions.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ─── Active Positions Header ──────────────────────────────────────────
        item {
            Text(
                text = "Active Positions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (positions.isEmpty()) {
            item {
                EmptyTradingState(modifier = Modifier.fillMaxWidth().padding(32.dp))
            }
        } else {
            items(positions, key = { it.id }) { coin ->
                PositionListItem(
                    coin = coin,
                    currencyFormatter = currencyFormatter,
                    onClick = { onCoinClick(coin.id) }
                )
                HorizontalDivider(
                    color = OutlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun BalanceHeroCard(
    totalBalance: Double,
    cashBalance: Double,
    pnlUsd: Double,
    pnlPercent: Double,
    isProfit: Boolean,
    currencyFormatter: NumberFormat
) {
    val pnlColor = if (isProfit) Color(0xFF00C853) else Color(0xFFFF5252)
    val pnlSign = if (isProfit) "+" else ""
    val gradientColors = if (isProfit)
        listOf(Color(0xFF0D1F12), Color(0xFF0A1628))
    else
        listOf(Color(0xFF1F0D0D), Color(0xFF0A1628))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradientColors))
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Total Portfolio Value",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currencyFormatter.format(totalBalance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = pnlColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format(
                        "%s%s (%s%.2f%%)",
                        pnlSign, currencyFormatter.format(pnlUsd),
                        pnlSign, pnlPercent
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = pnlColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unrealized PnL",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PositionListItem(
    coin: Coin,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val holdingsValue = coin.holdings * coin.currentPrice
    val costBasis = coin.holdings * coin.averageBuyPrice
    val pnlUsd = holdingsValue - costBasis
    val pnlPercent = if (costBasis > 0) (pnlUsd / costBasis) * 100 else 0.0
    val isProfit = pnlUsd >= 0
    val pnlSign = if (isProfit) "+" else ""

    val pnlColor by animateColorAsState(
        targetValue = if (isProfit) Positive else Negative,
        animationSpec = tween(300),
        label = "pnlColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Coin logo
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = "${coin.name} logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
        )

        // Name + holdings
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format("%.6f %s", coin.holdings, coin.symbol.uppercase()),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Value + PnL
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormatter.format(holdingsValue),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(pnlColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = String.format("%s%.2f%%", pnlSign, abs(pnlPercent)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = pnlColor
                )
            }
        }
    }
}

@Composable
private fun EmptyTradingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📈",
            fontSize = 56.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Active Positions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Go to a coin's detail page and\nhit Trade to start investing!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
