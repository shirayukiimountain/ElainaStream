package dev.shira.anime.ui.home

import dev.shira.anime.domain.model.AnimePost

data class HomeUiState(
    val posts: List<AnimePost> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
    val canLoadMore: Boolean = true
)
