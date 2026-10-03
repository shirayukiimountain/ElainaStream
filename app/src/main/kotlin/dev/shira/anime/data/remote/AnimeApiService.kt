package dev.shira.anime.data.remote

import dev.shira.anime.data.remote.dto.CategoryCollectionResponseDto
import dev.shira.anime.data.remote.dto.CategoryDetailResponseDto
import dev.shira.anime.data.remote.dto.GenreResponseDto
import dev.shira.anime.data.remote.dto.PostDescriptionResponseDto
import dev.shira.anime.data.remote.dto.PostsResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AnimeApiService {
    @GET("animexnonton/api/phalcon/api/get_anime_genre_list/")
    suspend fun getGenreList(): GenreResponseDto

    @FormUrlEncoded
    @POST("animexnonton/api/phalcon/api/get_posts/")
    suspend fun getPosts(
        @Field("isAPKvalid") isApkValid: Boolean = true,
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("device_id") deviceId: String,
        @Field("device_token") deviceToken: String
    ): PostsResponseDto

    @FormUrlEncoded
    @POST("animexnonton/api/phalcon/api/search_category_collection/")
    suspend fun searchCategoryCollection(
        @Field("isAPKvalid") isApkValid: Boolean = true,
        @Field("search") query: String,
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("lang") language: String = "ID"
    ): CategoryCollectionResponseDto

    @FormUrlEncoded
    @POST("animexnonton/api/phalcon/api/get_anime_by_genre/")
    suspend fun getAnimeByGenre(
        @Field("isAPKvalid") isApkValid: Boolean = true,
        @Field("genre1") genre1: String,
        @Field("genre2") genre2: String,
        @Field("lang") language: String = "ID",
        @Field("sort") sort: String = "ASC"
    ): CategoryCollectionResponseDto

    @FormUrlEncoded
    @POST("animexnonton/api/phalcon/api/get_category_posts_secure/")
    suspend fun getCategoryPosts(
        @Field("id") categoryId: Int,
        @Field("isAPKvalid") isApkValid: Boolean = true
    ): CategoryDetailResponseDto

    @FormUrlEncoded
    @POST("animexnonton/api/phalcon/api/get_post_description/")
    suspend fun getPostDescription(
        @Field("channel_id") channelId: Int,
        @Field("isAPKvalid") isApkValid: Boolean = true
    ): PostDescriptionResponseDto
}
