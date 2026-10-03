package dev.shira.anime.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GenreResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("count_total") val countTotal: Int?,
    @SerializedName("genre") val genres: List<GenreDto>?
)

data class GenreDto(
    @SerializedName("genre_id") val genreId: Int?,
    @SerializedName("genre_name") val genreName: String?,
    @SerializedName("genre_status_hide") val genreStatusHide: Int?
)
