package dev.shira.anime.domain.model

data class EpisodeDetail(
    val channelId: Int,
    val categoryId: Int,
    val title: String,
    val animeTitle: String,
    val descriptionHtml: String,
    val imageUrl: String,
    val videoUrl: String,
    val videoUrlHd: String,
    val videoUrlFhd: String,
    val isHdAvailable: Boolean,
    val isFhdAvailable: Boolean,
    val language: String,
    val rating: String,
    val year: String,
    val viewCount: String
)
