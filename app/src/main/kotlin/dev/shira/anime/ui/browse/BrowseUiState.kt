package dev.shira.anime.ui.browse

import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.Genre

data class SearchUiState(
    val query: String = "",
    val results: List<AnimeCollectionItem> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
    val canLoadMore: Boolean = false,
    val hasSearched: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

data class GenreUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenre: Genre? = null,
    val results: List<AnimeCollectionItem> = emptyList(),
    val isLoadingGenres: Boolean = true,
    val isLoadingResults: Boolean = false,
    val errorMessage: String? = null
)
