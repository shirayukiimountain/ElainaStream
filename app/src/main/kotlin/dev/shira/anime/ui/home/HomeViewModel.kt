package dev.shira.anime.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.shira.anime.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private var currentPage = FIRST_PAGE
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        currentPage = FIRST_PAGE
        _homeState.value = HomeUiState(isInitialLoading = true)

        loadJob = viewModelScope.launch {
            runCatching {
                repository.getLatestPosts(page = FIRST_PAGE, count = PAGE_SIZE)
            }.onSuccess { posts ->
                _homeState.value = HomeUiState(
                    posts = posts,
                    isInitialLoading = false,
                    canLoadMore = posts.size >= PAGE_SIZE
                )
            }.onFailure { throwable ->
                _homeState.value = HomeUiState(
                    isInitialLoading = false,
                    errorMessage = throwable.message ?: "Gagal memuat anime terbaru"
                )
            }
        }
    }

    fun loadNextPage() {
        val state = _homeState.value
        if (state.isInitialLoading || state.isLoadingMore || !state.canLoadMore) return

        _homeState.value = state.copy(
            isLoadingMore = true,
            loadMoreErrorMessage = null
        )

        val nextPage = currentPage + 1
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            runCatching {
                repository.getLatestPosts(page = nextPage, count = PAGE_SIZE)
            }.onSuccess { newPosts ->
                currentPage = nextPage
                val mergedPosts = (_homeState.value.posts + newPosts).distinctBy {
                    if (it.channelId != 0) it.channelId else it.categoryId
                }
                _homeState.value = _homeState.value.copy(
                    posts = mergedPosts,
                    isLoadingMore = false,
                    canLoadMore = newPosts.size >= PAGE_SIZE,
                    loadMoreErrorMessage = null
                )
            }.onFailure { throwable ->
                _homeState.value = _homeState.value.copy(
                    isLoadingMore = false,
                    loadMoreErrorMessage = throwable.message ?: "Gagal memuat halaman berikutnya"
                )
            }
        }
    }

    class Factory(
        private val repository: AnimeRepository = AnimeRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 22
    }
}
