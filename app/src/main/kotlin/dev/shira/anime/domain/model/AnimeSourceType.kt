package dev.shira.anime.domain.model

import androidx.annotation.DrawableRes
import dev.shira.anime.R

enum class AnimeSourceType(
    val id: String,
    val displayName: String,
    val tagLine: String,
    val serverBadge: String,
    val features: List<String>,
    @DrawableRes val imageResId: Int
) {
    ANIMEX(
        id = "animex",
        displayName = "AnimeX Cloud",
        tagLine = "Koleksi Anime Lengkap & Update Cepat",
        serverBadge = "Server 1",
        features = listOf("Sub Indo", "Multi-Res"),
        imageResId = R.drawable.elaina
    ),
    ANIMEKU(
        id = "animeku",
        displayName = "Animeku Fast CDN",
        tagLine = "Streaming Ultra Cepat • 1080p Direct",
        serverBadge = "Server 2",
        features = listOf("1080p FHD", "Sub Indo & Eng"),
        imageResId = R.drawable.elaina_2
    );

    companion object {
        fun fromId(id: String?): AnimeSourceType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ANIMEX
        }
    }
}
