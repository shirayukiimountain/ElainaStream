package dev.shira.anime.data.local

import android.content.Context
import dev.shira.anime.domain.model.AnimeSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SourcePreferences {
    private const val PREFS_NAME = "anime_source_preferences"
    private const val KEY_ACTIVE_SOURCE = "active_source_id"

    private val _currentSource = MutableStateFlow(AnimeSourceType.ANIMEX)
    val currentSource: StateFlow<AnimeSourceType> = _currentSource.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_ACTIVE_SOURCE, AnimeSourceType.ANIMEX.id)
        _currentSource.value = AnimeSourceType.fromId(savedId)
    }

    fun getSource(context: Context): AnimeSourceType {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_ACTIVE_SOURCE, AnimeSourceType.ANIMEX.id)
        val source = AnimeSourceType.fromId(savedId)
        _currentSource.value = source
        return source
    }

    fun setSource(context: Context, source: AnimeSourceType) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_SOURCE, source.id).apply()
        _currentSource.value = source
    }
}
