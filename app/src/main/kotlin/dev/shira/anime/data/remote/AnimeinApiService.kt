package dev.shira.anime.data.remote

import dev.shira.anime.data.remote.dto.animein.AnimeinBaseResponseDto
import dev.shira.anime.data.remote.dto.animein.AnimeinEpisodeListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinGenreListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinMovieDetailDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinMovieListDataDto
import dev.shira.anime.data.remote.dto.animein.AnimeinStreamDataDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeinApiService {

    @GET("data/home/list_new_episode")
    suspend fun getLatestEpisodes(
        @Query("limit") limit: Int = 27,
        @Query("genre_in") genreIn: String = ""
    ): AnimeinBaseResponseDto<AnimeinMovieListDataDto>

    @GET("3/2/explore/movie")
    suspend fun searchMovies(
        @Query("keyword") keyword: String = "",
        @Query("genre_in") genreIn: String = "",
        @Query("page") page: Int = 0,
        @Query("sort") sort: String = "views"
    ): AnimeinBaseResponseDto<AnimeinMovieListDataDto>

    @GET("3/2/movie/detail/{movieId}")
    suspend fun getMovieDetail(
        @Path("movieId") movieId: Int
    ): AnimeinBaseResponseDto<AnimeinMovieDetailDataDto>

    @GET("3/2/movie/episode/{movieId}")
    suspend fun getMovieEpisodes(
        @Path("movieId") movieId: Int
    ): AnimeinBaseResponseDto<AnimeinEpisodeListDataDto>

    @GET("3/2/episode/streamnew/{episodeId}")
    suspend fun getStream(
        @Path("episodeId") episodeId: Int
    ): AnimeinBaseResponseDto<AnimeinStreamDataDto>

    @GET("3/2/explore/genre")
    suspend fun getGenres(): AnimeinBaseResponseDto<AnimeinGenreListDataDto>

    companion object {
        const val BASE_URL = "https://xyz-api.animein.net/"
        const val USER_ID = "950960"
        const val CLIENT_KEY = "Z5ij9xqIj3qiyiUOEWaEaTkTZDISogBdPcbCPV0BHGzwFQZHUn"
        const val APK_VER = "5.2.2"
    }
}
