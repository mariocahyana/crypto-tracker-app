package com.example.gamestorehb.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.model.Sentiment
import com.example.gamestorehb.ui.components.EmptyView
import com.example.gamestorehb.ui.components.ErrorView
import com.example.gamestorehb.ui.components.LoadingIndicator
import com.example.gamestorehb.ui.theme.*
import com.example.gamestorehb.util.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val state by viewModel.newsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Market News", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchNews() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
                        )
                    }
                }
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
                is UiState.Empty -> EmptyView(subtitle = "No news available at the moment.")
                is UiState.Error -> ErrorView(message = uiState.message, onRetry = { viewModel.fetchNews() })
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(uiState.data, key = { it.id }) { article ->
                            NewsArticleItem(
                                article = article,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsArticleItem(
    article: NewsArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column {
            // Thumbnail
            AsyncImage(
                model = article.imageUrl,
                contentDescription = "News Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(OutlineVariant)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Source & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = article.sourceImageUrl,
                            contentDescription = "Source Logo",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = article.sourceName,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = formatTimeAgo(article.publishedOn),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sentiment Badge
                SentimentBadge(sentiment = article.sentiment)
            }
        }
    }
}

@Composable
private fun SentimentBadge(sentiment: Sentiment) {
    val (text, color, icon) = when (sentiment) {
        Sentiment.BULLISH -> Triple("Bullish", Positive, "🚀")
        Sentiment.BEARISH -> Triple("Bearish", Negative, "📉")
        Sentiment.NEUTRAL -> Triple("Neutral", TextSecondary, "➖")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun formatTimeAgo(timestampSeconds: Long): String {
    val diffMillis = System.currentTimeMillis() - (timestampSeconds * 1000)
    val hours = (diffMillis / (1000 * 60 * 60)).toInt()
    
    return when {
        hours == 0 -> "Just now"
        hours == 1 -> "1h ago"
        hours < 24 -> "${hours}h ago"
        else -> {
            val days = hours / 24
            "${days}d ago"
        }
    }
}
