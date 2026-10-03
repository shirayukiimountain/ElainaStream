package dev.shira.anime.data.repository

import dev.shira.anime.data.remote.AnimeApiService
import dev.shira.anime.data.remote.CryptoUtils
import dev.shira.anime.data.remote.NetworkModule
import dev.shira.anime.data.remote.dto.CategoryCollectionItemDto
import dev.shira.anime.data.remote.dto.CategoryDetailResponseDto
import dev.shira.anime.data.remote.dto.GenreDto
import dev.shira.anime.data.remote.dto.PostDescriptionResponseDto
import dev.shira.anime.data.remote.dto.PostDto
import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.domain.model.Genre

class AnimeXSource(
    private val apiService: AnimeApiService = NetworkModule.animeApiService
) : AnimeSource {
    override val sourceType: AnimeSourceType = AnimeSourceType.ANIMEX

    override suspend fun getLatestPosts(page: Int, count: Int): List<AnimePost> {
        return apiService.getPosts(
            page = page,
            count = count,
            deviceId = DEVICE_ID,
            deviceToken = DEVICE_TOKEN
        ).posts.orEmpty().mapNotNull { it.toAnimePost() }
    }

    override suspend fun getGenres(): List<Genre> {
        return apiService.getGenreList().genres.orEmpty().mapNotNull { it.toGenre() }
    }

    override suspend fun searchAnime(query: String, page: Int, count: Int): List<AnimeCollectionItem> {
        return apiService.searchCategoryCollection(
            query = query,
            page = page,
            count = count
        ).categories.orEmpty().mapNotNull { it.toAnimeCollectionItem() }
    }

    override suspend fun getAnimeByGenre(genreName: String): List<AnimeCollectionItem> {
        return apiService.getAnimeByGenre(
            genre1 = genreName,
            genre2 = genreName
        ).categories.orEmpty().mapNotNull { it.toAnimeCollectionItem() }
    }

    override suspend fun getAnimeCategory(categoryId: Int): AnimeCategory? {
        return apiService.getCategoryPosts(categoryId = categoryId).toAnimeCategory()
    }

    override suspend fun getEpisodeDetail(channelId: Int): EpisodeDetail? {
        return apiService.getPostDescription(channelId = channelId).toEpisodeDetail()
    }

    private fun PostDto.toAnimePost(): AnimePost? {
        val safeChannelId = channelId ?: return null
        val safeCategoryId = categoryId ?: return null

        return AnimePost(
            channelId = safeChannelId,
            categoryId = safeCategoryId,
            title = channelName.orEmpty(),
            animeTitle = categoryName.orEmpty().trim(),
            imageUrl = imageUrl.orEmpty(),
            createdAt = created.orEmpty(),
            viewCount = countView.orEmpty(),
            isOngoing = ongoing == 1,
            isHdAvailable = isHdAvailable == true,
            isFhdAvailable = isFhdAvailable == true,
            language = language.orEmpty()
        )
    }

    private fun GenreDto.toGenre(): Genre? {
        val safeId = genreId ?: return null
        val safeName = genreName?.takeIf { it.isNotBlank() } ?: return null
        if (genreStatusHide == 1) return null
        return Genre(id = safeId, name = safeName)
    }

    private fun CategoryCollectionItemDto.toAnimeCollectionItem(): AnimeCollectionItem? {
        val safeId = id ?: return null
        val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

        return AnimeCollectionItem(
            id = safeId,
            title = safeTitle.trim(),
            imageUrl = imageUrl.orEmpty(),
            episodeCount = episodeCount.orEmpty(),
            rating = rating.orEmpty(),
            year = year.orEmpty(),
            totalViews = totalViews ?: 0L,
            isOngoing = ongoing == 1,
            language = language.orEmpty()
        )
    }

    private fun CategoryDetailResponseDto.toAnimeCategory(): AnimeCategory? {
        val safeCategory = category ?: return null
        val safeCategoryId = safeCategory.cid ?: return null

        return AnimeCategory(
            id = safeCategoryId,
            title = safeCategory.categoryName.orEmpty(),
            imageUrl = safeCategory.imageUrl.orEmpty(),
            isOngoing = safeCategory.ongoing == 1,
            genre = safeCategory.genre.orEmpty(),
            year = safeCategory.years.orEmpty(),
            rating = safeCategory.rating.orEmpty(),
            episodes = posts.orEmpty().mapNotNull { episode ->
                val channelId = episode.channelId ?: return@mapNotNull null
                AnimeEpisode(
                    channelId = channelId,
                    categoryId = episode.categoryId ?: episode.legacyCategoryId ?: safeCategoryId,
                    title = episode.channelName.orEmpty(),
                    viewCount = episode.countView.orEmpty(),
                    isHdAvailable = episode.isHdAvailable == true,
                    isFhdAvailable = episode.isFhdAvailable == true
                )
            }
        )
    }

    private fun PostDescriptionResponseDto.toEpisodeDetail(): EpisodeDetail? {
        val safeChannelId = channelId ?: return null
        val key = secretKey.orEmpty()

        val rawUrl = CryptoUtils.decrypt(channelUrl, key)
        val rawUrlHd = CryptoUtils.decrypt(channelUrlHd, key)
        val rawUrlFhd = CryptoUtils.decrypt(channelUrlFhd, key)
        val rawUrlOri = CryptoUtils.decrypt(channelUrlOriginal, key)
        val rawUrlHdOri = CryptoUtils.decrypt(channelUrlHdOriginal, key)
        val rawUrlFhdOri = CryptoUtils.decrypt(channelUrlFhdOriginal, key)

        return EpisodeDetail(
            channelId = safeChannelId,
            categoryId = categoryId?.toIntOrNull() ?: 0,
            title = channelName.orEmpty(),
            animeTitle = categoryName.orEmpty(),
            descriptionHtml = channelDescription.orEmpty(),
            imageUrl = imageUrl.orEmpty(),
            videoUrl = rawUrl.ifBlank { rawUrlOri },
            videoUrlHd = rawUrlHd.ifBlank { rawUrlHdOri },
            videoUrlFhd = rawUrlFhd.ifBlank { rawUrlFhdOri },
            isHdAvailable = isHdAvailable == true,
            isFhdAvailable = isFhdAvailable == true,
            language = language.orEmpty(),
            rating = rating.orEmpty(),
            year = years?.toString().orEmpty(),
            viewCount = countView.orEmpty()
        )
    }

    private companion object {
        private const val DEVICE_ID = "7e0005ce5bf18f8e"
        private const val DEVICE_TOKEN = "7e0005ce5bf18f8e"
    }
}
