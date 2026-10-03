package dev.shira.anime.data.remote

import dev.shira.anime.data.remote.dto.animeku.AnimekuAnimeDetailResponseDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuCategoriesResponseDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuCategoryPostsResponseDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuPostDetailResponseDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuSearchResponseDto
import dev.shira.anime.data.remote.dto.animeku.AnimekuVideosResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimekuApiService {
    @GET("get_videos")
    suspend fun getVideos(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuVideosResponseDto

    @GET("get_category_popular")
    suspend fun getPopularCategories(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuCategoriesResponseDto

    @GET("get_category_new")
    suspend fun getNewCategories(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuCategoriesResponseDto

    @GET("get_category_complete")
    suspend fun getCompleteCategories(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuCategoriesResponseDto

    @GET("get_search_results")
    suspend fun searchResults(
        @Query("search") query: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuSearchResponseDto

    @GET("get_anime_detail")
    suspend fun getAnimeDetail(
        @Query("id") id: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuAnimeDetailResponseDto

    @GET("get_category_posts")
    suspend fun getCategoryPosts(
        @Query("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuCategoryPostsResponseDto

    @GET("get_post_detail")
    suspend fun getPostDetail(
        @Query("id") id: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): AnimekuPostDetailResponseDto

    companion object {
        const val BASE_URL = "https://pencarinafkah.xyz/vA6//api/"
        const val IMAGE_BASE_URL = "http://elara.whatbox.ca:29318/Duljanah/"
        const val API_KEY = "cda11y63tfI7rwln8BLeiKTvjsD5g2Mox01RzkhQCEXSGWbqYO"
    }
}
