package com.example.gamestorehb.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamestorehb.domain.model.Coin
import com.example.gamestorehb.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Reusable, single row composable for displaying a cryptocurrency asset.
 * Used in both [HomeScreen] and [PortfolioScreen].
 *
 * Displays: rank, icon, name/symbol, current price, 24h change badge.
 */
@Composable
fun CoinListItem(
    coin: Coin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priceChange = coin.priceChangePercentage24h
    val isPositive = priceChange >= 0

    val changeColor by animateColorAsState(
        targetValue = if (isPositive) Positive else Negative,
        animationSpec = tween(durationMillis = 300),
        label = "priceChangeColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Rank Number ─────────────────────────────────────────────────────
        Text(
            text = "#${coin.marketCapRank}",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            modifier = Modifier.width(28.dp)
        )

        // ── Coin Logo ────────────────────────────────────────────────────────
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = "${coin.name} logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
        )

        // ── Name & Symbol ────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = coin.symbol,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // ── Price & Change ───────────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(coin.currentPrice),
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 24h change badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(changeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${if (isPositive) "▲" else "▼"} ${String.format(Locale.US, "%.2f", abs(priceChange))}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = changeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return when {
        price >= 1_000 -> formatter.format(price).replace(",", ",")
        price >= 1 -> formatter.format(price)
        else -> "$${String.format(Locale.US, "%.6f", price)}"
    }
}
