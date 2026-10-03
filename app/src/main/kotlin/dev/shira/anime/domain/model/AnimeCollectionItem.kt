package dev.shira.anime.domain.model

data class AnimeCollectionItem(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val episodeCount: String,
    val rating: String,
    val year: String,
    val totalViews: Long,
    val isOngoing: Boolean,
    val language: String
)
