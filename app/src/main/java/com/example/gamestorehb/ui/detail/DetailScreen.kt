package com.example.gamestorehb.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.ui.components.ErrorView
import com.example.gamestorehb.ui.components.LineChart
import com.example.gamestorehb.ui.components.LoadingIndicator
import com.example.gamestorehb.util.UiState
import java.text.NumberFormat
import java.util.Locale

import com.example.gamestorehb.domain.usecase.TradeType
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val virtualBalance by viewModel.virtualBalance.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bookmarkMessage by viewModel.bookmarkMessage.collectAsStateWithLifecycle()
    var showTradeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(bookmarkMessage) {
        bookmarkMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBookmarkMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state is UiState.Success) {
                        val coin = (state as UiState.Success).data.coin
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = if (coin.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Toggle Bookmark",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (state is UiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = { showTradeSheet = true },
                    icon = { Icon(Icons.Filled.SwapHoriz, "Trade") },
                    text = { Text("Trade") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("trade_fab")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val uiState = state) {
                is UiState.Loading -> LoadingIndicator()
                is UiState.Empty -> {} // Should not happen for details
                is UiState.Error -> {
                    ErrorView(
                        message = uiState.message,
                        onRetry = { viewModel.loadCoinDetail() }
                    )
                }
                is UiState.Success -> {
                    DetailContent(
                        coin = uiState.data.coin,
                        history = uiState.data.historyPrices
                    )
                    if (showTradeSheet) {
                        TradeBottomSheet(
                            coin = uiState.data.coin,
                            virtualBalance = virtualBalance,
                            onDismiss = { showTradeSheet = false },
                            onTrade = { type, amount ->
                                viewModel.tradeCoin(type, amount)
                                showTradeSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(coin: Coin, history: List<Double>) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = coin.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = coin.symbol.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = currencyFormatter.format(coin.currentPrice),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val color = if (coin.priceChangePercentage24h >= 0) 
            MaterialTheme.colorScheme.primary 
        else MaterialTheme.colorScheme.error

        Text(
            text = String.format("%s%.2f%% (24h)", if (coin.priceChangePercentage24h >= 0) "+" else "", coin.priceChangePercentage24h),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (history.isNotEmpty()) {
            Text(
                text = "30-Day Trend",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            LineChart(
                data = history,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Market Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Market Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                StatRow(label = "Market Cap", value = currencyFormatter.format(coin.marketCap))
                StatRow(label = "Rank", value = "#${coin.marketCapRank}")
                StatRow(label = "Volume (24h)", value = currencyFormatter.format(coin.totalVolume))
                StatRow(label = "Circulating Supply", value = String.format("%,.0f", coin.circulatingSupply))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Holdings Stats (Paper Trading)
        if (coin.holdings > 0.0) {
            val totalValue = coin.holdings * coin.currentPrice
            val totalCost = coin.holdings * coin.averageBuyPrice
            val pnlUsd = totalValue - totalCost
            val pnlPercent = if (totalCost > 0) (pnlUsd / totalCost) * 100 else 0.0
            
            val pnlColor = if (pnlUsd >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            val pnlSign = if (pnlUsd >= 0) "+" else ""

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    StatRow(label = "Holdings", value = String.format("%.6f %s", coin.holdings, coin.symbol.uppercase()))
                    StatRow(label = "Average Buy Price", value = currencyFormatter.format(coin.averageBuyPrice))
                    StatRow(label = "Total Value", value = currencyFormatter.format(totalValue))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Unrealized PnL",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%s%s (%s%.2f%%)", pnlSign, currencyFormatter.format(pnlUsd), pnlSign, pnlPercent),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = pnlColor
                        )
                    }
                }
            }
        }
        
        // Add extra padding at bottom so FAB doesn't cover content
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TradeBottomSheet(
    coin: Coin,
    virtualBalance: Double,
    onDismiss: () -> Unit,
    onTrade: (TradeType, Double) -> Unit
) {
    var isBuy by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Trade ${coin.symbol}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buy / Sell Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = isBuy,
                    onClick = { isBuy = true },
                    label = { Text("Buy") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = !isBuy,
                    onClick = { isBuy = false },
                    label = { Text("Sell") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error,
                        selectedLabelColor = MaterialTheme.colorScheme.onError
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount in USD") },
                prefix = { Text("$") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trade_amount_field")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val available = if (isBuy) virtualBalance else coin.holdings * coin.currentPrice
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
            
            Text(
                text = "Available: ${currencyFormatter.format(available)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onTrade(if (isBuy) TradeType.BUY else TradeType.SELL, amount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(if (isBuy) "confirm_buy_button" else "confirm_sell_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (isBuy) "Confirm Buy" else "Confirm Sell")
            }
        }
    }
}
