package dev.shira.anime.ui.detail

import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.ui.player.VideoQuality

data class AnimeDetailUiModel(
    val category: AnimeCategory?,
    val episode: EpisodeDetail?,
    val fallbackTitle: String,
    val fallbackImageUrl: String
) {
    val animeTitle: String
        get() = category?.title?.takeIf { it.isNotBlank() }
            ?: episode?.animeTitle?.takeIf { it.isNotBlank() }
            ?: fallbackTitle

    val episodeTitle: String
        get() = episode?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle

    val imageUrl: String
        get() = category?.imageUrl?.takeIf { it.isNotBlank() }
            ?: episode?.imageUrl?.takeIf { it.isNotBlank() }
            ?: fallbackImageUrl

    val genre: String
        get() = category?.genre?.takeIf { it.isNotBlank() } ?: "-"

    val year: String
        get() = category?.year?.takeIf { it.isNotBlank() }
            ?: episode?.year?.takeIf { it.isNotBlank() }
            ?: "-"

    val rating: String
        get() = category?.rating?.takeIf { it.isNotBlank() }
            ?: episode?.rating?.takeIf { it.isNotBlank() }
            ?: "-"

    val status: String
        get() = if (category?.isOngoing == true) "Ongoing" else "Complete"

    val language: String
        get() = episode?.language?.takeIf { it.isNotBlank() } ?: "-"

    val viewCount: String
        get() = episode?.viewCount?.takeIf { it.isNotBlank() } ?: "0"

    val description: String
        get() = episode?.descriptionHtml?.takeIf { it.isNotBlank() } ?: "Deskripsi belum tersedia."

    val episodes: List<AnimeEpisode>
        get() = category?.episodes.orEmpty()

    val selectedChannelId: Int
        get() = episode?.channelId ?: 0

    val availableQualities: List<VideoQuality>
        get() {
            val safeEpisode = episode ?: return emptyList()
            return buildList {
                if (safeEpisode.videoUrl.isNotBlank()) {
                    add(VideoQuality(label = "360p", url = safeEpisode.videoUrl))
                }
                if (safeEpisode.videoUrlHd.isNotBlank()) {
                    add(VideoQuality(label = "720p", url = safeEpisode.videoUrlHd))
                }
                if (safeEpisode.videoUrlFhd.isNotBlank() && safeEpisode.videoUrlFhd != safeEpisode.videoUrlHd) {
                    add(VideoQuality(label = "1080p", url = safeEpisode.videoUrlFhd))
                }
            }
        }

    val bestVideoUrl: String
        get() = episode?.videoUrlFhd?.takeIf { it.isNotBlank() }
            ?: episode?.videoUrlHd?.takeIf { it.isNotBlank() }
            ?: episode?.videoUrl?.takeIf { it.isNotBlank() }
            ?: availableQualities.lastOrNull()?.url.orEmpty()
}
