package dev.shira.anime.data.remote.dto.animeku

import com.google.gson.annotations.SerializedName

data class AnimekuVideosResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("count_total") val countTotal: Int? = null,
    @SerializedName("pages") val pages: Int? = null,
    @SerializedName("latest_anime") val latestAnime: List<AnimekuVideoItemDto>? = null
)

data class AnimekuCategoriesResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("count_total") val countTotal: Int? = null,
    @SerializedName("pages") val pages: Int? = null,
    @SerializedName("new_anime") val newAnime: List<AnimekuCategoryItemDto>? = null
)

data class AnimekuCategoryPostsResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("count_total") val countTotal: Int? = null,
    @SerializedName("pages") val pages: Int? = null,
    @SerializedName("category") val category: AnimekuCategoryDetailDto? = null,
    @SerializedName("posts") val posts: List<AnimekuVideoItemDto>? = null
)

data class AnimekuAnimeDetailResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("category") val category: AnimekuCategoryDetailDto? = null,
    @SerializedName("tag_anime") val tagAnime: List<AnimekuCategoryItemDto>? = null,
    @SerializedName("suggested") val suggested: List<AnimekuVideoItemDto>? = null
)

data class AnimekuPostDetailResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("post") val post: AnimekuVideoItemDto? = null,
    @SerializedName("tag_anime") val tagAnime: List<AnimekuCategoryItemDto>? = null,
    @SerializedName("suggested") val suggested: List<AnimekuVideoItemDto>? = null
)

data class AnimekuSearchResponseDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("count_total") val countTotal: Int? = null,
    @SerializedName("pages") val pages: Int? = null,
    @SerializedName("posts") val posts: List<AnimekuVideoItemDto>? = null
)

data class AnimekuVideoItemDto(
    @SerializedName("vid") val vid: Int? = null,
    @SerializedName("cat_id") val catId: Int? = null,
    @SerializedName("video_title") val videoTitle: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("video_url_minihd") val videoUrlMiniHd: String? = null,
    @SerializedName("video_url_hd") val videoUrlHd: String? = null,
    @SerializedName("video_url_fullhd") val videoUrlFullHd: String? = null,
    @SerializedName("video_url_eng") val videoUrlEng: String? = null,
    @SerializedName("video_url_hdeng") val videoUrlHdEng: String? = null,
    @SerializedName("video_id") val videoId: String? = null,
    @SerializedName("video_thumbnail") val videoThumbnail: String? = null,
    @SerializedName("video_duration") val videoDuration: String? = null,
    @SerializedName("video_description") val videoDescription: String? = null,
    @SerializedName("video_type") val videoType: String? = null,
    @SerializedName("size") val size: String? = null,
    @SerializedName("total_views") val totalViews: Any? = null,
    @SerializedName("date_time") val dateTime: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("category_image") val categoryImage: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("rating") val rating: Any? = null,
    @SerializedName("year") val year: Any? = null,
    @SerializedName("status_video") val statusVideo: String? = null,
    @SerializedName("days") val days: Any? = null,
    @SerializedName("tag_anime") val tagAnime: String? = null
)

data class AnimekuCategoryItemDto(
    @SerializedName("cid") val cid: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("category_image") val categoryImage: String? = null,
    @SerializedName("status_video") val statusVideo: String? = null,
    @SerializedName("rating") val rating: Any? = null,
    @SerializedName("year") val year: Any? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("desc_anime") val descAnime: String? = null,
    @SerializedName("days") val days: Any? = null,
    @SerializedName("total_views") val totalViews: Any? = null,
    @SerializedName("total_mylist") val totalMylist: Any? = null,
    @SerializedName("video_count") val videoCount: Int? = null
)

data class AnimekuCategoryDetailDto(
    @SerializedName("cid") val cid: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("category_image") val categoryImage: String? = null,
    @SerializedName("status_video") val statusVideo: String? = null,
    @SerializedName("rating") val rating: Any? = null,
    @SerializedName("year") val year: Any? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("desc_anime") val descAnime: String? = null,
    @SerializedName("days") val days: Any? = null,
    @SerializedName("total_views") val totalViews: Any? = null,
    @SerializedName("video_count") val videoCount: Int? = null,
    @SerializedName("tag_anime") val tagAnime: String? = null
)
