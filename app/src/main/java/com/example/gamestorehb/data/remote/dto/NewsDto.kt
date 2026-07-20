package com.example.gamestorehb.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NewsResponseDto(
    @SerializedName("status")
    val status: String,
    @SerializedName("items")
    val items: List<RssItemDto>
)

data class RssItemDto(
    @SerializedName("title")
    val title: String?,
    @SerializedName("pubDate")
    val pubDate: String?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("author")
    val author: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("enclosure")
    val enclosure: EnclosureDto?
)

data class EnclosureDto(
    @SerializedName("link")
    val link: String?
)
