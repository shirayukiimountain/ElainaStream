package dev.shira.anime.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.shira.anime.domain.model.AnimeSourceType

class PlaybackProgressStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Any()
    @Volatile
    private var cachedHistory: MutableList<ContinueWatchingItem>? = null

    fun saveProgress(item: ContinueWatchingItem) {
        if (!item.hasProgress) return

        val trimmedList: List<ContinueWatchingItem>
        synchronized(lock) {
            val historyList = getOrLoadHistoryLocked()
            // Remove existing item if same channelId or same title
            historyList.removeAll { it.channelId == item.channelId || (it.categoryId == item.categoryId && it.title == item.title) }
            historyList.add(0, item)
            // Keep max 50 items
            trimmedList = if (historyList.size > 50) historyList.subList(0, 50).toList() else historyList.toList()
            cachedHistory = trimmedList.toMutableList()
        }
        val json = gson.toJson(trimmedList)

        preferences.edit()
            .putString(KEY_HISTORY_JSON, json)
            .putInt(KEY_CHANNEL_ID, item.channelId)
            .putInt(KEY_CATEGORY_ID, item.categoryId)
            .putString(KEY_TITLE, item.title)
            .putString(KEY_IMAGE_URL, item.imageUrl)
            .putLong(KEY_POSITION_MS, item.positionMs)
            .putLong(KEY_DURATION_MS, item.durationMs)
            .putLong(KEY_UPDATED_AT_MS, item.updatedAtMs)
            .putString(KEY_SOURCE_TYPE, item.sourceType.id)
            .putLong(positionKey(item.channelId), item.positionMs)
            .apply()
    }

    fun getLastContinueWatching(): ContinueWatchingItem? {
        val list = getAllHistory()
        if (list.isNotEmpty()) {
            return list.first()
        }

        val channelId = preferences.getInt(KEY_CHANNEL_ID, 0)
        val categoryId = preferences.getInt(KEY_CATEGORY_ID, 0)
        if (channelId == 0 || categoryId == 0) return null

        return ContinueWatchingItem(
            channelId = channelId,
            categoryId = categoryId,
            title = preferences.getString(KEY_TITLE, null).orEmpty(),
            imageUrl = preferences.getString(KEY_IMAGE_URL, null).orEmpty(),
            positionMs = preferences.getLong(KEY_POSITION_MS, 0L),
            durationMs = preferences.getLong(KEY_DURATION_MS, 0L),
            updatedAtMs = preferences.getLong(KEY_UPDATED_AT_MS, 0L),
            sourceType = AnimeSourceType.fromId(preferences.getString(KEY_SOURCE_TYPE, AnimeSourceType.ANIMEX.id))
        ).takeIf { it.hasProgress }
    }

    fun getAllHistory(): List<ContinueWatchingItem> {
        synchronized(lock) {
            return getOrLoadHistoryLocked().toList()
        }
    }

    private fun getOrLoadHistoryLocked(): MutableList<ContinueWatchingItem> {
        cachedHistory?.let { return it }
        val json = preferences.getString(KEY_HISTORY_JSON, null)
        val list = if (json.isNullOrBlank()) {
            mutableListOf()
        } else {
            try {
                val type = object : TypeToken<List<ContinueWatchingItem>>() {}.type
                val parsed = gson.fromJson<List<ContinueWatchingItem>>(json, type)
                parsed?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        }
        cachedHistory = list
        return list
    }

    fun removeHistoryItem(channelId: Int) {
        val trimmedList: List<ContinueWatchingItem>
        synchronized(lock) {
            val list = getOrLoadHistoryLocked()
            list.removeAll { it.channelId == channelId }
            trimmedList = list.toList()
            cachedHistory = list
        }
        preferences.edit()
            .putString(KEY_HISTORY_JSON, gson.toJson(trimmedList))
            .remove(positionKey(channelId))
            .apply()
    }

    fun clearHistory() {
        synchronized(lock) {
            cachedHistory?.clear()
        }
        preferences.edit().clear().apply()
    }

    fun getPosition(channelId: Int): Long {
        synchronized(lock) {
            cachedHistory?.firstOrNull { it.channelId == channelId }?.let {
                return it.positionMs
            }
        }
        return preferences.getLong(positionKey(channelId), 0L)
    }

    private fun positionKey(channelId: Int): String = "$KEY_POSITION_PREFIX$channelId"

    private companion object {
        const val PREFERENCES_NAME = "playback_progress"
        const val KEY_HISTORY_JSON = "history_json"
        const val KEY_CHANNEL_ID = "channel_id"
        const val KEY_CATEGORY_ID = "category_id"
        const val KEY_TITLE = "title"
        const val KEY_IMAGE_URL = "image_url"
        const val KEY_POSITION_MS = "position_ms"
        const val KEY_DURATION_MS = "duration_ms"
        const val KEY_UPDATED_AT_MS = "updated_at_ms"
        const val KEY_SOURCE_TYPE = "source_type"
        const val KEY_POSITION_PREFIX = "position_"
    }
}
