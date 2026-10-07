package dev.shira.anime.data.repository

import dev.shira.anime.data.local.SourcePreferences
import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.domain.model.Genre

class AnimeRepository(
    private val animexSource: AnimeSource = AnimeXSource(),
    private val animekuSource: AnimeSource = AnimekuSource(),
    private val animeinSource: AnimeSource = AnimeinSource()
) {
    fun getActiveSource(): AnimeSource {
        return when (SourcePreferences.currentSource.value) {
            AnimeSourceType.ANIMEX -> animexSource
            AnimeSourceType.ANIMEKU -> animekuSource
            AnimeSourceType.ANIMEIN -> animeinSource
        }
    }

    suspend fun getLatestPosts(page: Int = 1, count: Int = 20): List<AnimePost> {
        return getActiveSource().getLatestPosts(page = page, count = count)
    }

    suspend fun getGenres(): List<Genre> {
        return getActiveSource().getGenres()
    }

    suspend fun searchAnime(query: String, page: Int = 1, count: Int = 20): List<AnimeCollectionItem> {
        return getActiveSource().searchAnime(query = query, page = page, count = count)
    }

    suspend fun getAnimeByGenre(genreName: String): List<AnimeCollectionItem> {
        return getActiveSource().getAnimeByGenre(genreName = genreName)
    }

    suspend fun getAnimeCategory(categoryId: Int): AnimeCategory? {
        return getActiveSource().getAnimeCategory(categoryId = categoryId)
    }

    suspend fun getEpisodeDetail(channelId: Int): EpisodeDetail? {
        return getActiveSource().getEpisodeDetail(channelId = channelId)
    }
}
