package dev.shira.anime.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostDescriptionResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("channel_description") val channelDescription: String?,
    @SerializedName("channel_name") val channelName: String?,
    @SerializedName("channel_image") val channelImage: String?,
    @SerializedName("channel_id") val channelId: Int?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("channel_url") val channelUrl: String?,
    @SerializedName("channel_url_hd") val channelUrlHd: String?,
    @SerializedName("channel_url_fhd") val channelUrlFhd: String?,
    @SerializedName("is_hd_available") val isHdAvailable: Boolean?,
    @SerializedName("is_fhd_available") val isFhdAvailable: Boolean?,
    @SerializedName("gdrive_url") val gdriveUrl: String?,
    @SerializedName("episode_url_1") val episodeUrl1: String?,
    @SerializedName("episode_url_2") val episodeUrl2: String?,
    @SerializedName("lang") val language: String?,
    @SerializedName("ongoing") val ongoing: Int?,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("allow_download") val allowDownload: Boolean?,
    @SerializedName("safe_images") val safeImages: Boolean?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("years") val years: Int?,
    @SerializedName("channel_url_ori") val channelUrlOriginal: String?,
    @SerializedName("channel_url_hd_ori") val channelUrlHdOriginal: String?,
    @SerializedName("channel_url_fhd_ori") val channelUrlFhdOriginal: String?,
    @SerializedName("count_view") val countView: String?,
    @SerializedName("secretKey") val secretKey: String? = null
)
