package dev.shira.anime.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.shira.anime.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPage = FIRST_PAGE

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = SearchUiState(query = _uiState.value.query)
            return
        }

        currentPage = FIRST_PAGE
        _uiState.value = _uiState.value.copy(
            isSearching = true,
            isLoadingMore = false,
            errorMessage = null,
            loadMoreErrorMessage = null,
            hasSearched = true,
            results = emptyList(),
            canLoadMore = false
        )

        viewModelScope.launch {
            runCatching {
                repository.searchAnime(query = query, page = FIRST_PAGE, count = PAGE_SIZE)
            }.onSuccess { results ->
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isSearching = false,
                    canLoadMore = results.size >= PAGE_SIZE
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = throwable.message ?: "Gagal mencari anime"
                )
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        val query = state.query.trim()
        if (query.isEmpty() || state.isSearching || state.isLoadingMore || !state.canLoadMore) return

        _uiState.value = state.copy(
            isLoadingMore = true,
            loadMoreErrorMessage = null
        )

        val nextPage = currentPage + 1
        viewModelScope.launch {
            runCatching {
                repository.searchAnime(query = query, page = nextPage, count = PAGE_SIZE)
            }.onSuccess { newResults ->
                currentPage = nextPage
                _uiState.value = _uiState.value.copy(
                    results = (_uiState.value.results + newResults).distinctBy { it.id },
                    isLoadingMore = false,
                    canLoadMore = newResults.size >= PAGE_SIZE
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    loadMoreErrorMessage = throwable.message ?: "Gagal memuat hasil berikutnya"
                )
            }
        }
    }

    class Factory(
        private val repository: AnimeRepository = AnimeRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20
    }
}
