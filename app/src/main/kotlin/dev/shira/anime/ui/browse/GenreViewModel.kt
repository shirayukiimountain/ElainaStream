package dev.shira.anime.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.shira.anime.data.repository.AnimeRepository
import dev.shira.anime.domain.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenreViewModel(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GenreUiState())
    val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

    init {
        loadGenres()
    }

    fun loadGenres() {
        _uiState.value = _uiState.value.copy(
            isLoadingGenres = true,
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                repository.getGenres()
            }.onSuccess { genres ->
                _uiState.value = _uiState.value.copy(
                    genres = genres,
                    isLoadingGenres = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoadingGenres = false,
                    errorMessage = throwable.message ?: "Gagal memuat genre"
                )
            }
        }
    }

    fun selectGenre(genre: Genre) {
        _uiState.value = _uiState.value.copy(
            selectedGenre = genre,
            results = emptyList(),
            isLoadingResults = true,
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching {
                repository.getAnimeByGenre(genre.name)
            }.onSuccess { results ->
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isLoadingResults = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoadingResults = false,
                    errorMessage = throwable.message ?: "Gagal memuat anime genre ${genre.name}"
                )
            }
        }
    }

    class Factory(
        private val repository: AnimeRepository = AnimeRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenreViewModel::class.java)) {
                return GenreViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
