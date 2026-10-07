package dev.shira.anime.data.remote.dto.animein

import com.google.gson.annotations.SerializedName

data class AnimeinBaseResponseDto<T>(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("error") val error: Boolean? = null,
    @SerializedName("data") val data: T? = null
)

data class AnimeinMovieListDataDto(
    @SerializedName("movie") val movie: List<AnimeinMovieDto>? = null
)

data class AnimeinMovieDetailDataDto(
    @SerializedName("movie") val movie: AnimeinMovieDto? = null,
    @SerializedName("episode") val episode: AnimeinEpisodeDto? = null
)

data class AnimeinEpisodeListDataDto(
    @SerializedName("episode") val episode: List<AnimeinEpisodeDto>? = null
)

data class AnimeinStreamDataDto(
    @SerializedName("episode") val episode: AnimeinEpisodeDto? = null,
    @SerializedName("episode_next") val episodeNext: AnimeinEpisodeDto? = null,
    @SerializedName("server") val server: List<AnimeinServerDto>? = null
)

data class AnimeinGenreListDataDto(
    @SerializedName("genre") val genre: List<AnimeinGenreDto>? = null
)

data class AnimeinMovieDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("synonyms") val synonyms: String? = null,
    @SerializedName("image_poster") val imagePoster: String? = null,
    @SerializedName("image_cover") val imageCover: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("day") val day: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("views") val views: String? = null,
    @SerializedName("favorites") val favorites: String? = null,
    @SerializedName("aired_start") val airedStart: String? = null,
    @SerializedName("key_time_update") val keyTimeUpdate: String? = null,
    @SerializedName("genre") val genre: String? = null
)

data class AnimeinEpisodeDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("index") val index: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("views") val views: String? = null,
    @SerializedName("id_movie") val idMovie: String? = null,
    @SerializedName("key_time") val keyTime: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("is_new") val isNew: String? = null
)

data class AnimeinServerDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("key_file_size") val keyFileSize: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("server_id") val serverId: String? = null
)

data class AnimeinGenreDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("group") val group: String? = null
)
