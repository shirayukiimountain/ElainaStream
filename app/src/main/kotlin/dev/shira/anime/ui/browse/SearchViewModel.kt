package dev.shira.anime.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.shira.anime.data.local.SearchHistoryStore
import dev.shira.anime.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: AnimeRepository,
    private val searchHistoryStore: SearchHistoryStore? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPage = FIRST_PAGE
    private var searchJob: Job? = null
    private var debounceJob: Job? = null

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        val history = searchHistoryStore?.getHistory().orEmpty()
        _uiState.value = _uiState.value.copy(recentSearches = history)
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        val trimmed = query.trim()

        if (trimmed.isEmpty()) {
            debounceJob?.cancel()
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                results = emptyList(),
                hasSearched = false,
                errorMessage = null,
                canLoadMore = false
            )
            return
        }

        // Debounce auto-search if query length >= 2
        if (trimmed.length >= 2) {
            debounceJob?.cancel()
            debounceJob = viewModelScope.launch {
                delay(DEBOUNCE_DELAY_MS)
                performSearch(query = trimmed, recordHistory = false)
            }
        }
    }

    fun search() {
        debounceJob?.cancel()
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(
                query = "",
                isSearching = false,
                results = emptyList(),
                hasSearched = false,
                errorMessage = null
            )
            return
        }
        performSearch(query = query, recordHistory = true)
    }

    fun onRecentSearchClicked(keyword: String) {
        debounceJob?.cancel()
        _uiState.value = _uiState.value.copy(query = keyword)
        performSearch(query = keyword.trim(), recordHistory = true)
    }

    fun removeRecentSearch(keyword: String) {
        searchHistoryStore?.removeSearch(keyword)
        loadRecentSearches()
    }

    fun clearRecentSearches() {
        searchHistoryStore?.clearAll()
        loadRecentSearches()
    }

    private fun performSearch(query: String, recordHistory: Boolean) {
        if (recordHistory && query.length >= 2) {
            searchHistoryStore?.addSearch(query)
            loadRecentSearches()
        }

        searchJob?.cancel()
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

        searchJob = viewModelScope.launch {
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
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
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
        private val repository: AnimeRepository = AnimeRepository(),
        private val searchHistoryStore: SearchHistoryStore? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel(repository, searchHistoryStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20
        const val DEBOUNCE_DELAY_MS = 500L
    }
}
