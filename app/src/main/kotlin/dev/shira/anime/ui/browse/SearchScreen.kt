package dev.shira.anime.ui.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.ui.theme.SakuraPulse
import dev.shira.anime.ui.theme.VoidBlack
import kotlinx.coroutines.flow.distinctUntilChanged

private val POPULAR_SEARCH_KEYWORDS = listOf(
    "Solo Leveling",
    "One Piece",
    "Jujutsu Kaisen",
    "Demon Slayer",
    "Chainsaw Man",
    "Bleach",
    "Attack on Titan",
    "Frieren",
    "Mushoku Tensei",
    "Oshi no Ko",
    "Wind Breaker",
    "Kaiju No. 8"
)

private val GENRE_SEARCH_KEYWORDS = listOf(
    "Action",
    "Isekai",
    "Romance",
    "Fantasy",
    "Donghua",
    "Sci-Fi",
    "Comedy",
    "Adventure",
    "Supernatural",
    "Mystery"
)

@Composable
fun SearchScreen(
    state: SearchUiState,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (AnimeCollectionItem) -> Unit,
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)

    LaunchedEffect(listState, state.results.size, state.canLoadMore, state.isLoadingMore) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleIndex >= totalItems - 5
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) onLoadMore()
            }
    }

    fun submitSearch(queryText: String? = null) {
        if (queryText != null) {
            onQueryChanged(queryText)
        }
        keyboardController?.hide()
        onSearch()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        // Modern Top Bar Header
        BrowseTopBar(
            title = "Pencarian",
            subtitle = "Temukan anime, donghua & movie favoritmu",
            onBack = onBack
        )

        // Modern Glassmorphic Pill Search Bar
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF141624),
            border = BorderStroke(
                1.dp,
                if (state.query.isNotEmpty()) accentIndigo.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Search Icon
                Surface(
                    shape = CircleShape,
                    color = accentIndigo.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = accentSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.query.isEmpty()) {
                        Text(
                            text = "Ketik judul anime, donghua...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMuted,
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = state.query,
                        onValueChange = onQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(accentSoft),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Clear button (if text exists)
                if (state.query.isNotEmpty()) {
                    Surface(
                        onClick = { onQueryChanged("") },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(2.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Hapus teks",
                                tint = textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Submit Button
                Surface(
                    onClick = { submitSearch() },
                    shape = RoundedCornerShape(12.dp),
                    color = accentIndigo,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Cari",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content Area
        when {
            // 1. Loading State
            state.isSearching -> {
                BrowseLoadingState(
                    message = if (state.query.isNotBlank()) "Mencari \"${state.query}\"..." else "Mencari anime...",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Error State
            state.errorMessage != null -> {
                BrowseMessageState(
                    title = "Gagal Mencari Anime",
                    message = state.errorMessage,
                    icon = Icons.Outlined.SearchOff,
                    actionText = "Coba Lagi",
                    onAction = onSearch,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Pre-Search / Empty State (Suggestions & Trending)
            !state.hasSearched -> {
                SearchSuggestionsContent(
                    recentSearches = state.recentSearches,
                    onKeywordSelected = { keyword ->
                        submitSearch(keyword)
                    },
                    onRemoveRecentSearch = onRemoveRecentSearch,
                    onClearRecentSearches = onClearRecentSearches
                )
            }

            // 4. Empty Result State
            state.results.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrowseMessageState(
                        title = "Tidak Ada Hasil",
                        message = "Tidak ditemukan anime dengan kata kunci \"${state.query}\". Silakan coba kata kunci lain atau pilih rekomendasi di bawah.",
                        icon = Icons.Outlined.SearchOff,
                        modifier = Modifier.weight(1f)
                    )

                    SearchSuggestionsContent(
                        recentSearches = state.recentSearches,
                        onKeywordSelected = { keyword ->
                            submitSearch(keyword)
                        },
                        onRemoveRecentSearch = onRemoveRecentSearch,
                        onClearRecentSearches = onClearRecentSearches,
                        showTips = false,
                        modifier = Modifier.padding(bottom = 100.dp)
                    )
                }
            }

            // 5. Search Results List
            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Results Counter Banner
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ditemukan ${state.results.size} anime",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )

                            if (state.query.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentIndigo.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, accentIndigo.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = "\"${state.query}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFA5B4FC),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Anime Collection Cards
                    items(state.results, key = { it.id }) { item ->
                        AnimeCollectionCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }

                    // Pagination Loader
                    if (state.isLoadingMore) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = SakuraPulse,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Memuat hasil berikutnya...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    // Pagination Error
                    state.loadMoreErrorMessage?.let { message ->
                        item {
                            BrowseMessageState(
                                title = "Gagal Memuat Lagi",
                                message = message,
                                actionText = "Coba Lagi",
                                onAction = onLoadMore
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionsContent(
    recentSearches: List<String> = emptyList(),
    onKeywordSelected: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    showTips: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)
    val cardBg = Color(0xFF131520)
    val cardBorder = Color(0xFF222536)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Section: Recent Searches
        if (recentSearches.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = accentSoft,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "PENCARIAN TERAKHIR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "Hapus Semua",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onClearRecentSearches() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(recentSearches) { query ->
                            RecentSearchChip(
                                text = query,
                                onClick = { onKeywordSelected(query) },
                                onDelete = { onRemoveRecentSearch(query) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Trending / Popular Searches
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PENCARIAN POPULER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        letterSpacing = 0.8.sp
                    )
                }

                // Horizontal list of trending anime chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(POPULAR_SEARCH_KEYWORDS) { keyword ->
                        SuggestionChip(
                            text = keyword,
                            icon = Icons.Outlined.LocalFireDepartment,
                            iconTint = Color(0xFFF87171),
                            onClick = { onKeywordSelected(keyword) }
                        )
                    }
                }
            }
        }

        // Section: Browse by Genre / Categories
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = accentSoft,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "KATEGORI & GENRE FAVORIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary,
                        letterSpacing = 0.8.sp
                    )
                }

                // Horizontal list of genre chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(GENRE_SEARCH_KEYWORDS) { genre ->
                        SuggestionChip(
                            text = genre,
                            icon = Icons.Outlined.AutoAwesome,
                            iconTint = accentSoft,
                            onClick = { onKeywordSelected(genre) }
                        )
                    }
                }
            }
        }

        // Section: Fast Search Tips Banner
        if (showTips) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Subtle Gradient Glow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accentIndigo.copy(alpha = 0.12f),
                                            Color.Transparent
                                        ),
                                        radius = 350f
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = accentIndigo.copy(alpha = 0.20f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = null,
                                            tint = accentSoft,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Tips Pencarian Cepat",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }

                            Text(
                                text = "Gunakan judul dalam bahasa Indonesia, Inggris, ataupun Romaji Jepang (misal: Kimetsu no Yaiba atau Demon Slayer) untuk hasil pencarian yang lebih akurat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }

        // Space at bottom for floating navigation bar
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    icon: ImageVector? = null,
    iconTint: Color = Color(0xFF818CF8),
    onClick: () -> Unit
) {
    val chipBg = Color(0xFF161826)
    val chipBorder = Color(0xFF24273A)
    val textPrimary = Color(0xFFF1F5F9)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = BorderStroke(1.dp, chipBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                fontSize = 12.5.sp
            )
        }
    }
}

@Composable
private fun RecentSearchChip(
    text: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipBg = Color(0xFF161826)
    val chipBorder = Color(0xFF24273A)
    val textPrimary = Color(0xFFF1F5F9)
    val textMuted = Color(0xFF94A3B8)
    val accentSoft = Color(0xFF818CF8)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = BorderStroke(1.dp, chipBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = accentSoft,
                modifier = Modifier.size(15.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = textPrimary,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                onClick = onDelete,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.06f),
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Hapus riwayat",
                        tint = textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
