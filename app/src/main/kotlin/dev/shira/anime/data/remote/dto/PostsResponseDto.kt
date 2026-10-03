package dev.shira.anime.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostsResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("count") val count: Int?,
    @SerializedName("count_total") val countTotal: Int?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("posts") val posts: List<PostDto>?
)

data class PostDto(
    @SerializedName("channel_id") val channelId: Int?,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("channel_name") val channelName: String?,
    @SerializedName("channel_image") val channelImage: String?,
    @SerializedName("channel_type") val channelType: String?,
    @SerializedName("video_id") val videoId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("created") val created: String?,
    @SerializedName("count_view") val countView: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("ongoing") val ongoing: Int?,
    @SerializedName("is_fhd_available") val isFhdAvailable: Boolean?,
    @SerializedName("is_hd_available") val isHdAvailable: Boolean?,
    @SerializedName("lang") val language: String?,
    @SerializedName("safe_images") val safeImages: Boolean?
)
