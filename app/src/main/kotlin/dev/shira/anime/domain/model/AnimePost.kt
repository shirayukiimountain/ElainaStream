package dev.shira.anime.domain.model

data class AnimePost(
    val channelId: Int,
    val categoryId: Int,
    val title: String,
    val animeTitle: String,
    val imageUrl: String,
    val createdAt: String,
    val viewCount: String,
    val isOngoing: Boolean,
    val isHdAvailable: Boolean,
    val isFhdAvailable: Boolean,
    val language: String
)
