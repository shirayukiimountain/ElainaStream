package dev.shira.anime.ui.detail

import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.shira.anime.data.repository.AnimeRepository
import dev.shira.anime.ui.common.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _detailState = MutableStateFlow<UiState<AnimeDetailUiModel>>(UiState.Loading)
    val detailState: StateFlow<UiState<AnimeDetailUiModel>> = _detailState.asStateFlow()

    private var loadJob: Job? = null

    fun load(
        channelId: Int,
        categoryId: Int,
        fallbackTitle: String,
        fallbackImageUrl: String
    ) {
        loadJob?.cancel()
        _detailState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val categoryDeferred = async { runCatching { repository.getAnimeCategory(categoryId) }.getOrNull() }
                    val episodeDeferred = async { runCatching { repository.getEpisodeDetail(channelId) }.getOrNull() }

                    val category = categoryDeferred.await()
                    val episode = episodeDeferred.await()

                    AnimeDetailUiModel(
                        category = category,
                        episode = episode?.copy(
                            descriptionHtml = cleanHtml(episode.descriptionHtml)
                        ),
                        fallbackTitle = fallbackTitle,
                        fallbackImageUrl = fallbackImageUrl
                    )
                }
            }.onSuccess { detail ->
                if (detail.category == null && detail.episode == null) {
                    _detailState.value = UiState.Error("Detail anime belum tersedia.")
                } else {
                    _detailState.value = UiState.Success(detail)
                }
            }.onFailure { throwable ->
                _detailState.value = UiState.Error(
                    throwable.message ?: "Gagal memuat detail anime"
                )
            }
        }
    }

    private fun cleanHtml(html: String): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    class Factory(
        private val repository: AnimeRepository = AnimeRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
