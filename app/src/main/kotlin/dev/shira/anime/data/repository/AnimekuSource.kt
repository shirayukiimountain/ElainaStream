package dev.shira.anime.data.repository

import dev.shira.anime.data.remote.AnimekuApiService
import dev.shira.anime.data.remote.NetworkModule
import dev.shira.anime.data.remote.dto.animeku.AnimekuCategoryDetailDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuCategoryItemDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuVideoItemDto
import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.domain.model.Genre

class AnimekuSource(
    private val apiService: AnimekuApiService = NetworkModule.animekuApiService
) : AnimeSource {
    override val sourceType: AnimeSourceType = AnimeSourceType.ANIMEKU

    override suspend fun getLatestPosts(page: Int, count: Int): List<AnimePost> {
        val response = apiService.getVideos(page = page, count = count)
        return response.latestAnime.orEmpty().mapNotNull { it.toAnimePost() }
    }

    override suspend fun getGenres(): List<Genre> {
        return listOf(
            Genre(id = 1, name = "Action"),
            Genre(id = 2, name = "Adventure"),
            Genre(id = 3, name = "Comedy"),
            Genre(id = 4, name = "Drama"),
            Genre(id = 5, name = "Fantasy"),
            Genre(id = 6, name = "Isekai"),
            Genre(id = 7, name = "Magic"),
            Genre(id = 8, name = "Martial Arts"),
            Genre(id = 9, name = "Romance"),
            Genre(id = 10, name = "Sci-Fi"),
            Genre(id = 11, name = "Shounen"),
            Genre(id = 12, name = "Slice of Life"),
            Genre(id = 13, name = "Supernatural"),
            Genre(id = 14, name = "Donghua")
        )
    }

    override suspend fun searchAnime(query: String, page: Int, count: Int): List<AnimeCollectionItem> {
        val response = apiService.searchResults(query = query, page = page, count = count)
        val items = response.posts.orEmpty().mapNotNull { it.toAnimeCollectionItem() }
        return items.distinctBy { it.id }
    }

    override suspend fun getAnimeByGenre(genreName: String): List<AnimeCollectionItem> {
        val response = apiService.getPopularCategories(page = 1, count = 50)
        return response.newAnime.orEmpty()
            .filter { it.genre?.contains(genreName, ignoreCase = true) == true }
            .mapNotNull { it.toAnimeCollectionItem() }
    }

    override suspend fun getAnimeCategory(categoryId: Int): AnimeCategory? {
        val detailResponse = runCatching { apiService.getAnimeDetail(id = categoryId) }.getOrNull()
        val postsResponse = runCatching { apiService.getCategoryPosts(id = categoryId, page = 1, count = 200) }.getOrNull()

        val categoryInfo = detailResponse?.category ?: postsResponse?.category ?: return null
        val episodesList = postsResponse?.posts.takeUnless { it.isNullOrEmpty() }
            ?: detailResponse?.suggested.orEmpty()

        val safeCid = categoryInfo.cid ?: categoryId

        return AnimeCategory(
            id = safeCid,
            title = categoryInfo.categoryName.orEmpty(),
            imageUrl = formatImageUrl(categoryInfo.categoryImage),
            isOngoing = categoryInfo.statusVideo.equals("OnGoing", ignoreCase = true),
            genre = categoryInfo.genre.orEmpty(),
            year = categoryInfo.year?.toString().orEmpty(),
            rating = categoryInfo.rating?.toString().orEmpty(),
            episodes = episodesList.mapNotNull { ep ->
                val vid = ep.vid ?: return@mapNotNull null
                AnimeEpisode(
                    channelId = vid,
                    categoryId = ep.catId ?: safeCid,
                    title = ep.videoTitle.orEmpty(),
                    viewCount = ep.totalViews?.toString().orEmpty(),
                    isHdAvailable = !ep.videoUrlHd.isNullOrBlank(),
                    isFhdAvailable = !ep.videoUrlFullHd.isNullOrBlank()
                )
            }
        )
    }

    override suspend fun getEpisodeDetail(channelId: Int): EpisodeDetail? {
        val response = apiService.getPostDetail(id = channelId)
        val post = response.post ?: return null
        val safeVid = post.vid ?: channelId

        return EpisodeDetail(
            channelId = safeVid,
            categoryId = post.catId ?: 0,
            title = post.videoTitle.orEmpty(),
            animeTitle = post.categoryName.orEmpty(),
            descriptionHtml = post.videoDescription.orEmpty(),
            imageUrl = formatImageUrl(post.categoryImage ?: post.videoThumbnail),
            videoUrl = post.videoUrl.orEmpty().ifBlank { post.videoUrlMiniHd.orEmpty() },
            videoUrlHd = post.videoUrlHd.orEmpty(),
            videoUrlFhd = post.videoUrlFullHd.orEmpty(),
            isHdAvailable = !post.videoUrlHd.isNullOrBlank(),
            isFhdAvailable = !post.videoUrlFullHd.isNullOrBlank(),
            language = "Sub Indo",
            rating = post.rating?.toString().orEmpty(),
            year = post.year?.toString().orEmpty(),
            viewCount = post.totalViews?.toString().orEmpty()
        )
    }

    private fun AnimekuVideoItemDto.toAnimePost(): AnimePost? {
        val safeVid = vid ?: return null
        val safeCatId = catId ?: return null

        return AnimePost(
            channelId = safeVid,
            categoryId = safeCatId,
            title = videoTitle.orEmpty(),
            animeTitle = categoryName.orEmpty().trim().ifBlank { videoTitle.orEmpty() },
            imageUrl = formatImageUrl(categoryImage ?: videoThumbnail),
            createdAt = dateTime.orEmpty(),
            viewCount = totalViews?.toString().orEmpty(),
            isOngoing = statusVideo.equals("OnGoing", ignoreCase = true),
            isHdAvailable = !videoUrlHd.isNullOrBlank(),
            isFhdAvailable = !videoUrlFullHd.isNullOrBlank(),
            language = "Sub Indo"
        )
    }

    private fun AnimekuVideoItemDto.toAnimeCollectionItem(): AnimeCollectionItem? {
        val safeId = catId ?: vid ?: return null
        val safeTitle = categoryName?.takeIf { it.isNotBlank() } ?: videoTitle?.takeIf { it.isNotBlank() } ?: return null

        return AnimeCollectionItem(
            id = safeId,
            title = safeTitle.trim(),
            imageUrl = formatImageUrl(categoryImage ?: videoThumbnail),
            episodeCount = "",
            rating = rating?.toString().orEmpty(),
            year = year?.toString().orEmpty(),
            totalViews = totalViews?.toString()?.toLongOrNull() ?: 0L,
            isOngoing = statusVideo.equals("OnGoing", ignoreCase = true),
            language = "Sub Indo"
        )
    }

    private fun AnimekuCategoryItemDto.toAnimeCollectionItem(): AnimeCollectionItem? {
        val safeId = cid ?: return null
        val safeTitle = categoryName?.takeIf { it.isNotBlank() } ?: return null

        return AnimeCollectionItem(
            id = safeId,
            title = safeTitle.trim(),
            imageUrl = formatImageUrl(categoryImage),
            episodeCount = videoCount?.toString().orEmpty(),
            rating = rating?.toString().orEmpty(),
            year = year?.toString().orEmpty(),
            totalViews = totalViews?.toString()?.toLongOrNull() ?: 0L,
            isOngoing = statusVideo.equals("OnGoing", ignoreCase = true),
            language = "Sub Indo"
        )
    }

    private fun formatImageUrl(image: String?): String {
        if (image.isNullOrBlank()) return ""
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image
        }
        return AnimekuApiService.IMAGE_BASE_URL + image.trimStart('/')
    }
}
