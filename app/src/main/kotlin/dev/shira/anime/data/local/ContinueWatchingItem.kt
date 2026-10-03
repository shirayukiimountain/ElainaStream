package dev.shira.anime.data.local

data class ContinueWatchingItem(
    val channelId: Int,
    val categoryId: Int,
    val title: String,
    val imageUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMs: Long
) {
    val hasProgress: Boolean
        get() = positionMs > MINIMUM_PROGRESS_MS

    val progressFraction: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    private companion object {
        const val MINIMUM_PROGRESS_MS = 10_000L
    }
}
