package dev.shira.anime.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var cachedHistory: List<String>? = null

    fun getHistory(): List<String> {
        cachedHistory?.let { return it }
        val json = preferences.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        val list = runCatching { gson.fromJson<List<String>>(json, type) }
            .getOrNull()
            .orEmpty()
        cachedHistory = list
        return list
    }

    fun addSearch(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) return

        val current = getHistory().toMutableList()
        current.removeAll { it.equals(cleanQuery, ignoreCase = true) }
        current.add(0, cleanQuery)
        val trimmed = current.take(MAX_HISTORY_ITEMS)

        cachedHistory = trimmed
        val json = gson.toJson(trimmed)
        preferences.edit().putString(KEY_HISTORY, json).apply()
    }

    fun removeSearch(query: String) {
        val current = getHistory().toMutableList()
        current.removeAll { it.equals(query.trim(), ignoreCase = true) }
        cachedHistory = current
        val json = gson.toJson(current)
        preferences.edit().putString(KEY_HISTORY, json).apply()
    }

    fun clearAll() {
        cachedHistory = emptyList()
        preferences.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val PREFS_NAME = "search_history_prefs"
        private const val KEY_HISTORY = "history_json"
        private const val MAX_HISTORY_ITEMS = 10
    }
}
