package dev.shira.anime.data.repository

import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.domain.model.Genre

interface AnimeSource {
    val sourceType: AnimeSourceType
    suspend fun getLatestPosts(page: Int = 1, count: Int = 20): List<AnimePost>
    suspend fun getGenres(): List<Genre>
    suspend fun searchAnime(query: String, page: Int = 1, count: Int = 20): List<AnimeCollectionItem>
    suspend fun getAnimeByGenre(genreName: String): List<AnimeCollectionItem>
    suspend fun getAnimeCategory(categoryId: Int): AnimeCategory?
    suspend fun getEpisodeDetail(channelId: Int): EpisodeDetail?
}
