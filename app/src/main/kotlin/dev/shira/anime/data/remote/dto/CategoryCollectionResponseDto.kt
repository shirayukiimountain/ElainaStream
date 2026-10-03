package dev.shira.anime.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryCollectionResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("count") val count: Int?,
    @SerializedName("count_total") val countTotal: Int?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("categories") val categories: List<CategoryCollectionItemDto>?
)

data class CategoryCollectionItemDto(
    @SerializedName("cid") val id: Int?,
    @SerializedName("category_name") val title: String?,
    @SerializedName("category_image") val categoryImage: String?,
    @SerializedName("count_anime") val episodeCount: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("days") val days: Int?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("years") val year: String?,
    @SerializedName("total_views") val totalViews: Long?,
    @SerializedName("ongoing") val ongoing: Int?,
    @SerializedName("lang") val language: String?,
    @SerializedName("safe_images") val safeImages: Boolean?
)
