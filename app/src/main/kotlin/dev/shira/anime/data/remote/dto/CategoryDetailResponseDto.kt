package dev.shira.anime.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDetailResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("count") val count: Int?,
    @SerializedName("count_total") val countTotal: Int?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("category") val category: CategoryDto?,
    @SerializedName("posts") val posts: List<CategoryEpisodeDto>?
)

data class CategoryDto(
    @SerializedName("cid") val cid: Int?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("ongoing") val ongoing: Int?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("years") val years: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("is_fhd_available") val isFhdAvailable: Boolean?,
    @SerializedName("is_hd_available") val isHdAvailable: Boolean?,
    @SerializedName("safe_images") val safeImages: Boolean?
)

data class CategoryEpisodeDto(
    @SerializedName("channel_id") val channelId: Int?,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("category_id,") val legacyCategoryId: Int?,
    @SerializedName("channel_name") val channelName: String?,
    @SerializedName("is_fhd_available") val isFhdAvailable: Boolean?,
    @SerializedName("is_hd_available") val isHdAvailable: Boolean?,
    @SerializedName("count_view") val countView: String?
)
