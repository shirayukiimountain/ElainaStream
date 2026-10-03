package dev.shira.anime.ui.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.Genre
import dev.shira.anime.ui.theme.VoidBlack

@Composable
fun GenreScreen(
    state: GenreUiState,
    onBack: () -> Unit,
    onRetryGenres: () -> Unit,
    onGenreSelected: (Genre) -> Unit,
    onItemClick: (AnimeCollectionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        BrowseTopBar(
            title = "Kategori & Genre",
            subtitle = "Jelajahi anime berdasarkan genre favorit",
            onBack = onBack
        )

        when {
            state.isLoadingGenres -> {
                BrowseLoadingState(
                    message = "Memuat daftar genre...",
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.errorMessage != null && state.genres.isEmpty() -> {
                BrowseMessageState(
                    title = "Gagal Memuat Genre",
                    message = state.errorMessage,
                    icon = Icons.Outlined.ErrorOutline,
                    actionText = "Coba Lagi",
                    onAction = onRetryGenres,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                // Genre Pill Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    items(state.genres, key = { it.id }) { genre ->
                        val selected = state.selectedGenre?.id == genre.id
                        GenrePill(
                            name = genre.name,
                            isSelected = selected,
                            onClick = { onGenreSelected(genre) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    state.isLoadingResults -> {
                        BrowseLoadingState(
                            message = "Memuat anime genre ${state.selectedGenre?.name.orEmpty()}...",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    state.selectedGenre == null -> {
                        BrowseMessageState(
                            title = "Pilih Genre Anime",
                            message = "Pilih salah satu genre di atas untuk melihat koleksi anime.",
                            icon = Icons.Outlined.Category,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    state.errorMessage != null -> {
                        BrowseMessageState(
                            title = "Gagal Memuat Anime",
                            message = state.errorMessage,
                            icon = Icons.Outlined.ErrorOutline,
                            actionText = "Coba Lagi",
                            onAction = { state.selectedGenre?.let(onGenreSelected) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    state.results.isEmpty() -> {
                        BrowseMessageState(
                            title = "Belum Ada Anime",
                            message = "Belum ada anime yang tersedia untuk genre ${state.selectedGenre?.name}.",
                            icon = Icons.Outlined.Category,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Menampilkan ${state.results.size} anime untuk genre ${state.selectedGenre?.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textSecondary,
                                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 2.dp)
                                )
                            }

                            items(state.results, key = { it.id }) { item ->
                                AnimeCollectionCard(
                                    item = item,
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenrePill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentIndigo = Color(0xFF6366F1)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentIndigo else Color(0xFF161826),
        label = "genrePillBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else textSecondary,
        label = "genrePillText"
    )
    val borderColor = if (isSelected) accentIndigo.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}
