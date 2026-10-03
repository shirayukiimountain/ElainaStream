package dev.shira.anime.domain.model

data class AnimeCategory(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val isOngoing: Boolean,
    val genre: String,
    val year: String,
    val rating: String,
    val episodes: List<AnimeEpisode>
)

data class AnimeEpisode(
    val channelId: Int,
    val categoryId: Int,
    val title: String,
    val viewCount: String,
    val isHdAvailable: Boolean,
    val isFhdAvailable: Boolean
)
