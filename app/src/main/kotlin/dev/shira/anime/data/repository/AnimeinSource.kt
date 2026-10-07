package dev.shira.anime.data.repository

import dev.shira.anime.data.remote.AnimeinApiService
import dev.shira.anime.data.remote.NetworkModule
import dev.shira.anime.data.remote.dto.animein.AnimeinMovieDto
import dev.shira.anime.domain.model.AnimeCategory
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.domain.model.EpisodeDetail
import dev.shira.anime.domain.model.Genre

class AnimeinSource(
    private val apiService: AnimeinApiService = NetworkModule.animeinApiService
) : AnimeSource {
    override val sourceType: AnimeSourceType = AnimeSourceType.ANIMEIN

    @Volatile
    private var cachedGenres: List<Genre> = emptyList()

    override suspend fun getLatestPosts(page: Int, count: Int): List<AnimePost> {
        val offset = (page - 1).coerceAtLeast(0) * count
        if (offset >= 100) return emptyList()

        val fetchLimit = (page * count).coerceIn(20, 100)
        val response = apiService.getLatestEpisodes(limit = fetchLimit)
        val movies = response.data?.movie.orEmpty()

        return movies.drop(offset).take(count).mapNotNull { it.toAnimePost() }
    }

    override suspend fun getGenres(): List<Genre> {
        val remoteGenres = runCatching {
            val response = apiService.getGenres()
            response.data?.genre.orEmpty().mapNotNull { dto ->
                val name = dto.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val id = dto.id?.toIntOrNull() ?: return@mapNotNull null
                Genre(id = id, name = name)
            }
        }.getOrNull()

        if (!remoteGenres.isNullOrEmpty()) {
            cachedGenres = remoteGenres
            return remoteGenres
        }

        if (cachedGenres.isNotEmpty()) {
            return cachedGenres
        }

        return DEFAULT_GENRES.also { cachedGenres = it }
    }

    override suspend fun searchAnime(query: String, page: Int, count: Int): List<AnimeCollectionItem> {
        val apiPage = (page - 1).coerceAtLeast(0)
        val response = apiService.searchMovies(keyword = query, page = apiPage)
        val items = response.data?.movie.orEmpty().mapNotNull { it.toAnimeCollectionItem() }
        return items.distinctBy { it.id }
    }

    override suspend fun getAnimeByGenre(genreName: String): List<AnimeCollectionItem> {
        val genres = cachedGenres.ifEmpty { getGenres() }
        val matchedGenre = genres.firstOrNull { it.name.equals(genreName, ignoreCase = true) }

        val movies = if (matchedGenre != null) {
            val response = apiService.searchMovies(genreIn = matchedGenre.id.toString(), page = 0)
            response.data?.movie.orEmpty()
        } else {
            val response = apiService.searchMovies(page = 0)
            response.data?.movie.orEmpty().filter {
                it.genre?.contains(genreName, ignoreCase = true) == true
            }
        }

        return movies.mapNotNull { it.toAnimeCollectionItem() }.distinctBy { it.id }
    }

    override suspend fun getAnimeCategory(categoryId: Int): AnimeCategory? {
        val detailResponse = runCatching { apiService.getMovieDetail(movieId = categoryId) }.getOrNull()
        val movie = detailResponse?.data?.movie ?: return null

        val episodesResponse = runCatching { apiService.getMovieEpisodes(movieId = categoryId) }.getOrNull()
        val episodeList = episodesResponse?.data?.episode.takeUnless { it.isNullOrEmpty() }
            ?: detailResponse.data?.episode?.let { listOf(it) }.orEmpty()

        val episodes = episodeList.mapNotNull { ep ->
            val epId = ep.id?.toIntOrNull() ?: return@mapNotNull null
            val epTitle = ep.title?.takeIf { it.isNotBlank() } ?: "Episode ${ep.index.orEmpty()}".trim()
            AnimeEpisode(
                channelId = epId,
                categoryId = categoryId,
                title = epTitle,
                viewCount = ep.views.orEmpty(),
                isHdAvailable = true,
                isFhdAvailable = true
            )
        }

        return AnimeCategory(
            id = categoryId,
            title = movie.title.orEmpty(),
            imageUrl = movie.imagePoster ?: movie.imageCover.orEmpty(),
            isOngoing = movie.status.equals("ONGOING", ignoreCase = true),
            genre = movie.genre.orEmpty(),
            year = movie.year.orEmpty(),
            rating = movie.favorites.orEmpty(),
            episodes = episodes,
            description = movie.synopsis.orEmpty()
        )
    }

    override suspend fun getEpisodeDetail(channelId: Int): EpisodeDetail? {
        if (channelId <= 0) return null

        val streamResponse = runCatching { apiService.getStream(episodeId = channelId) }.getOrNull()
        val streamData = streamResponse?.data ?: return null
        val episode = streamData.episode ?: return null

        val movieId = episode.idMovie?.toIntOrNull()
        val movieDetail = movieId?.let {
            runCatching { apiService.getMovieDetail(movieId = it) }.getOrNull()?.data?.movie
        }

        val servers = streamData.server.orEmpty()
        val directServers = servers.filter {
            it.type.equals("direct", ignoreCase = true) || it.link?.contains(".mp4") == true
        }.ifEmpty {
            servers.filter { it.link?.contains("/embed/") != true }
        }

        val fhdServer = directServers.firstOrNull { it.quality?.contains("1080") == true }
        val hdServer = directServers.firstOrNull { it.quality?.contains("720") == true }
        val sd480Server = directServers.firstOrNull { it.quality?.contains("480") == true }
        val sd360Server = directServers.firstOrNull { it.quality?.contains("360") == true }

        val videoUrlFhd = fhdServer?.link.orEmpty()
        val videoUrlHd = hdServer?.link.orEmpty()
        val videoUrl = (sd360Server ?: sd480Server ?: hdServer ?: fhdServer ?: directServers.firstOrNull())?.link.orEmpty()

        val animeTitle = movieDetail?.title?.takeIf { it.isNotBlank() } ?: episode.title.orEmpty()
        val epTitle = episode.title?.takeIf { it.isNotBlank() } ?: "Episode ${episode.index.orEmpty()}".trim()
        val imageUrl = movieDetail?.imagePoster ?: movieDetail?.imageCover ?: episode.image.orEmpty()

        return EpisodeDetail(
            channelId = channelId,
            categoryId = movieId ?: 0,
            title = epTitle,
            animeTitle = animeTitle,
            descriptionHtml = movieDetail?.synopsis.orEmpty(),
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            videoUrlHd = videoUrlHd,
            videoUrlFhd = videoUrlFhd,
            isHdAvailable = videoUrlHd.isNotBlank(),
            isFhdAvailable = videoUrlFhd.isNotBlank(),
            language = "Sub Indo",
            rating = movieDetail?.favorites.orEmpty(),
            year = movieDetail?.year.orEmpty(),
            viewCount = episode.views.orEmpty()
        )
    }

    private fun AnimeinMovieDto.toAnimePost(): AnimePost? {
        val safeCatId = id?.toIntOrNull() ?: return null
        val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

        return AnimePost(
            channelId = 0,
            categoryId = safeCatId,
            title = safeTitle,
            animeTitle = safeTitle,
            imageUrl = imagePoster ?: imageCover.orEmpty(),
            createdAt = keyTimeUpdate.orEmpty(),
            viewCount = views.orEmpty(),
            isOngoing = status.equals("ONGOING", ignoreCase = true),
            isHdAvailable = true,
            isFhdAvailable = true,
            language = "Sub Indo"
        )
    }

    private fun AnimeinMovieDto.toAnimeCollectionItem(): AnimeCollectionItem? {
        val safeId = id?.toIntOrNull() ?: return null
        val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

        return AnimeCollectionItem(
            id = safeId,
            title = safeTitle.trim(),
            imageUrl = imagePoster ?: imageCover.orEmpty(),
            episodeCount = "",
            rating = favorites.orEmpty(),
            year = year.orEmpty(),
            totalViews = views?.toLongOrNull() ?: 0L,
            isOngoing = status.equals("ONGOING", ignoreCase = true),
            language = "Sub Indo"
        )
    }

    companion object {
        private val DEFAULT_GENRES = listOf(
            Genre(id = 14, name = "Action"),
            Genre(id = 16, name = "Adventure"),
            Genre(id = 25, name = "Comedy"),
            Genre(id = 20, name = "Drama"),
            Genre(id = 17, name = "Ecchi"),
            Genre(id = 13, name = "Fantasy"),
            Genre(id = 37, name = "Game"),
            Genre(id = 29, name = "Harem"),
            Genre(id = 18, name = "Historical"),
            Genre(id = 35, name = "Horror"),
            Genre(id = 30, name = "Magic"),
            Genre(id = 15, name = "Martial Arts"),
            Genre(id = 40, name = "Mecha"),
            Genre(id = 36, name = "Military"),
            Genre(id = 33, name = "Music"),
            Genre(id = 21, name = "Mystery"),
            Genre(id = 23, name = "Psychological"),
            Genre(id = 19, name = "Romance"),
            Genre(id = 26, name = "School"),
            Genre(id = 31, name = "Sci-Fi"),
            Genre(id = 32, name = "Shounen"),
            Genre(id = 27, name = "Slice of Life"),
            Genre(id = 42, name = "Sports"),
            Genre(id = 24, name = "Supernatural"),
            Genre(id = 34, name = "Thriller")
        )
    }
}
