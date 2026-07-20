package com.example.gamestorehb.data.repository

import com.example.gamestorehb.domain.model.NewsArticle
import com.example.gamestorehb.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Fetches real-time crypto news by directly parsing CoinTelegraph's RSS feed.
 * Avoids third-party proxies (and their HTTP 429 rate limits) entirely.
 *
 * Bug fix: XmlPullParser.name returns null for TEXT events, so we track
 * the current tag with a dedicated [currentTag] variable updated on START_TAG.
 */
class NewsRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : NewsRepository {

    private val RSS_URL = "https://cointelegraph.com/rss"

    // Cointelegraph RSS uses RFC-2822: "Sun, 29 Jun 2026 04:00:00 +0000"
    private val rssDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

    override fun getNews(): Flow<List<NewsArticle>> = flow {
        val articles = withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(RSS_URL)
                    .header("User-Agent", "CryptoTrackerApp/1.0")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                parseRss(body)
            } catch (e: Exception) {
                emptyList()
            }
        }
        emit(articles)
    }

    private fun parseRss(xml: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()

        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = true
        }
        val parser = factory.newPullParser()
        parser.setInput(xml.byteInputStream(), "UTF-8")

        var inItem = false
        // ── Key fix: track current tag here, NOT from parser.name on TEXT events ──
        var currentTag = ""

        // Per-item field accumulators (use StringBuilder for multi-chunk TEXT)
        val titleBuf = StringBuilder()
        val linkBuf = StringBuilder()
        val pubDateBuf = StringBuilder()
        val descBuf = StringBuilder()
        val authorBuf = StringBuilder()
        var imageUrl = ""

        fun resetItem() {
            titleBuf.clear(); linkBuf.clear(); pubDateBuf.clear()
            descBuf.clear(); authorBuf.clear(); imageUrl = ""
        }

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name ?: ""
                    when (currentTag) {
                        "item" -> {
                            inItem = true
                            resetItem()
                        }
                        // Image URL is an attribute on <enclosure> or <media:content>
                        "enclosure", "media:content" -> {
                            if (inItem) {
                                val url = parser.getAttributeValue(null, "url") ?: ""
                                if (url.isNotBlank()) imageUrl = url
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val endTag = parser.name ?: ""
                    if (endTag == "item" && inItem) {
                        inItem = false
                        val title = titleBuf.toString().trim()
                        val link = linkBuf.toString().trim()
                        if (title.isNotBlank() && link.isNotBlank()) {
                            val publishedSeconds = try {
                                rssDateFormat.parse(pubDateBuf.toString().trim())
                                    ?.time?.div(1000) ?: (System.currentTimeMillis() / 1000)
                            } catch (e: Exception) {
                                System.currentTimeMillis() / 1000
                            }

                            val finalImage = imageUrl.takeIf { it.isNotBlank() }
                                ?: "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?auto=format&fit=crop&w=600&q=80"

                            val bodyText = android.text.Html
                                .fromHtml(descBuf.toString(), android.text.Html.FROM_HTML_MODE_COMPACT)
                                .toString().trim()

                            articles.add(
                                NewsArticle(
                                    id = link,
                                    publishedOn = publishedSeconds,
                                    imageUrl = finalImage,
                                    title = title,
                                    url = link,
                                    body = bodyText,
                                    sourceName = authorBuf.toString().trim().ifBlank { "Cointelegraph" },
                                    sourceImageUrl = "https://s3.cointelegraph.com/storage/uploads/view/b9ea15d46738b4df64e6e100ab59b373.png"
                                )
                            )
                        }
                    }
                    // Reset currentTag so leftover text doesn't bleed into the wrong field
                    currentTag = ""
                }

                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    if (inItem && currentTag.isNotBlank()) {
                        val text = parser.text ?: ""
                        when (currentTag) {
                            "title"                -> titleBuf.append(text)
                            "link"                 -> linkBuf.append(text)
                            "pubDate"              -> pubDateBuf.append(text)
                            "description", "content:encoded" -> descBuf.append(text)
                            "dc:creator", "author" -> authorBuf.append(text)
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return articles.take(20)
    }
}
