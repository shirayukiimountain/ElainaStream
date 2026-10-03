package dev.shira.anime.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.ui.common.UiState
import dev.shira.anime.ui.home.LoadingState
import dev.shira.anime.ui.home.MessageState
import dev.shira.anime.ui.theme.AmberFrame
import dev.shira.anime.ui.theme.ElevatedPanel
import dev.shira.anime.ui.theme.InkPanel
import dev.shira.anime.ui.theme.LineDark
import dev.shira.anime.ui.theme.MistText
import dev.shira.anime.ui.theme.MoonText
import dev.shira.anime.ui.theme.SakuraPulse
import dev.shira.anime.ui.theme.SurfaceHighest
import dev.shira.anime.ui.theme.SurfaceLowest
import dev.shira.anime.ui.theme.VioletSignal
import dev.shira.anime.ui.theme.VoidBlack

@Composable
fun DetailScreen(
    state: UiState<AnimeDetailUiModel>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEpisodeClick: (AnimeEpisode) -> Unit,
    onWatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        when (state) {
            UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
            is UiState.Error -> MessageState(
                title = "Detail belum bisa dibuka",
                message = state.message,
                actionText = "Coba lagi",
                onAction = onRetry,
                modifier = Modifier.fillMaxSize()
            )

            is UiState.Success -> DetailContent(
                detail = state.data,
                onBack = onBack,
                onEpisodeClick = onEpisodeClick,
                onWatchClick = onWatchClick
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: AnimeDetailUiModel,
    onBack: () -> Unit,
    onEpisodeClick: (AnimeEpisode) -> Unit,
    onWatchClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Hero Poster & Quick Header
        item {
            DetailHero(
                detail = detail,
                onBack = onBack,
                onWatchClick = { onWatchClick(detail.bestVideoUrl) }
            )
        }

        // 2. Main Content Body
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Redesigned Glassmorphic Synopsis Section
                DetailSynopsisCard(
                    synopsis = detail.description,
                    genres = detail.genre.split(",").map { it.trim() }.filter { it.isNotBlank() && it != "-" }
                )

                // Anime Metadata Info Card
                AnimeMetadataCard(detail = detail)

                // Episode List Section
                if (detail.episodes.isNotEmpty()) {
                    EpisodeSection(
                        detail = detail,
                        onEpisodeClick = onEpisodeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHero(
    detail: AnimeDetailUiModel,
    onBack: () -> Unit,
    onWatchClick: () -> Unit
) {
    val accentIndigo = Color(0xFF6366F1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(574.dp)
    ) {
        AsyncImage(
            model = detail.imageUrl,
            contentDescription = detail.animeTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoidBlack.copy(alpha = 0.35f),
                            Color.Transparent,
                            VoidBlack.copy(alpha = 0.75f),
                            VoidBlack
                        )
                    )
                )
        )

        // Top Action Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassActionButton(
                image = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Kembali",
                onClick = onBack
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassActionButton(
                    image = Icons.Outlined.Share,
                    contentDescription = "Bagikan",
                    onClick = {}
                )
                GlassActionButton(
                    image = Icons.Outlined.Add,
                    contentDescription = "Bookmark",
                    onClick = {}
                )
            }
        }

        // Hero Info Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailTag(
                    text = detail.status,
                    containerColor = if (detail.status.contains("Ongoing", ignoreCase = true)) {
                        Color(0xFF10B981).copy(alpha = 0.20f)
                    } else {
                        accentIndigo.copy(alpha = 0.25f)
                    },
                    contentColor = if (detail.status.contains("Ongoing", ignoreCase = true)) {
                        Color(0xFF6EE7B7)
                    } else {
                        Color(0xFFA5B4FC)
                    },
                    borderColor = if (detail.status.contains("Ongoing", ignoreCase = true)) {
                        Color(0xFF10B981).copy(alpha = 0.40f)
                    } else {
                        accentIndigo.copy(alpha = 0.45f)
                    }
                )

                detail.genre
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && it != "-" }
                    .take(2)
                    .forEach { genre ->
                        DetailTag(
                            text = genre,
                            containerColor = Color(0xFF161826).copy(alpha = 0.85f),
                            contentColor = MoonText,
                            borderColor = Color.White.copy(alpha = 0.10f)
                        )
                    }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = buildHeroTitle(detail.animeTitle),
                style = MaterialTheme.typography.displaySmall,
                color = MoonText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingMeta(rating = detail.rating)
                InlineMeta(text = detail.year)
                InlineMeta(text = buildSeasonLabel(detail))
                InlineMeta(text = buildEpisodeCountLabel(detail))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWatchClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tonton Sekarang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Tambah ke daftar",
                            tint = MoonText
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailSynopsisCard(
    synopsis: String,
    genres: List<String>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "arrowRotation"
    )

    val cardBg = Color(0xFF141624)
    val cardBorder = Color(0xFF222538)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)

    val isLongSynopsis = synopsis.length > 200

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize(animationSpec = tween(280)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Icon & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentIndigo.copy(alpha = 0.18f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = accentSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Sinopsis Cerita",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            // Synopsis Body Text
            Text(
                text = synopsis.ifBlank { "Sinopsis cerita untuk anime ini belum tersedia." },
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary,
                lineHeight = 22.sp,
                maxLines = if (isExpanded || !isLongSynopsis) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )

            // Genre Tag Chips
            if (genres.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    genres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1C1F32),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA5B4FC),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Expand / Collapse Action
            if (isLongSynopsis) {
                Surface(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Tampilkan Lebih Sedikit" else "Baca Selengkapnya",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accentSoft
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = accentSoft,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeMetadataCard(
    detail: AnimeDetailUiModel,
    modifier: Modifier = Modifier
) {
    val cardBg = Color(0xFF141624)
    val cardBorder = Color(0xFF222538)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentIndigo.copy(alpha = 0.18f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = accentSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Informasi Tambahan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            // Grid Items
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetadataRow(
                    icon = Icons.Outlined.Movie,
                    label = "Total Episode",
                    value = if (detail.episodes.isNotEmpty()) "${detail.episodes.size} Episode" else "-"
                )
                MetadataRow(
                    icon = Icons.Outlined.CalendarToday,
                    label = "Tahun Rilis",
                    value = detail.year.ifBlank { "-" }
                )
                MetadataRow(
                    icon = Icons.Outlined.Translate,
                    label = "Audio / Bahasa",
                    value = detail.language.ifBlank { "Subtitle Indonesia" }
                )
                MetadataRow(
                    icon = Icons.Rounded.Star,
                    label = "Rating Skor",
                    value = "★ ${detail.rating.ifBlank { "4.8" }}"
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentSoft = Color(0xFF818CF8)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentSoft,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
    }
}

@Composable
private fun GlassActionButton(
    image: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFF141624).copy(alpha = 0.65f),
        contentColor = MoonText,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = image,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DetailTag(
    text: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RatingMeta(rating: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = AmberFrame,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = rating.ifBlank { "4.8" },
            style = MaterialTheme.typography.labelMedium,
            color = MoonText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InlineMeta(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MistText
    )
}

@Composable
private fun SectionTitle(title: String) {
    val accentIndigo = Color(0xFF6366F1)
    val textPrimary = Color(0xFFF1F5F9)

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentIndigo)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
    }
}

private const val EPISODE_CHUNK_SIZE = 50

private data class DisplayEpisode(
    val episode: AnimeEpisode,
    val episodeNumber: Int,
    val chronologicalIndex: Int
)

private data class EpisodeChunkInfo(
    val chunkIndex: Int,
    val startEpisodeNum: Int,
    val endEpisodeNum: Int,
    val label: String,
    val hasCurrentSelected: Boolean
)

private fun parseEpisodeNumber(title: String): Int? {
    if (title.isBlank()) return null
    // 1. Match prefixes like "Episode 1050", "Eps 1050", "Ep 1050", "Ep. 1050"
    val epPrefixRegex = Regex("""(?i)(?:episode|eps|ep)\.?\s*(\d+)""")
    epPrefixRegex.find(title)?.let { match ->
        return match.groupValues[1].toIntOrNull()
    }

    // 2. Match "#1050", "- 1050", ": 1050"
    val delimRegex = Regex("""[#:\-]\s*(\d+)\b""")
    delimRegex.find(title)?.let { match ->
        return match.groupValues[1].toIntOrNull()
    }

    // 3. Match standalone numbers (ignoring likely years 1970..2040)
    val numberMatches = Regex("""\b(\d+)\b""").findAll(title)
        .mapNotNull { it.groupValues[1].toIntOrNull() }
        .filter { it !in 1970..2040 }
        .toList()
    return numberMatches.firstOrNull()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EpisodeSection(
    detail: AnimeDetailUiModel,
    onEpisodeClick: (AnimeEpisode) -> Unit
) {
    val totalEpisodeCount = detail.episodes.size
    var isReversedOrder by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(totalEpisodeCount > 40) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Detect if raw list from API is sorted Descending (e.g. latest episode at index 0)
    val isRawDescending = remember(detail.episodes) {
        if (detail.episodes.size <= 1) false
        else {
            val firstNum = parseEpisodeNumber(detail.episodes.first().title)
            val lastNum = parseEpisodeNumber(detail.episodes.last().title)
            if (firstNum != null && lastNum != null) {
                firstNum > lastNum
            } else {
                detail.episodes.first().channelId > detail.episodes.last().channelId
            }
        }
    }

    // Master list in true chronological order (Episode 1 -> Episode N)
    val chronologicalEpisodes = remember(detail.episodes, isRawDescending) {
        val rawList = detail.episodes
        val baseList = if (isRawDescending) rawList.reversed() else rawList
        baseList.mapIndexed { index, ep ->
            val epNum = parseEpisodeNumber(ep.title) ?: (index + 1)
            DisplayEpisode(
                episode = ep,
                episodeNumber = epNum,
                chronologicalIndex = index
            )
        }
    }

    // Ordered episodes according to sort direction:
    // isReversedOrder == false -> "Terlama" (Ascending: 1 -> N)
    // isReversedOrder == true  -> "Terbaru" (Descending: N -> 1)
    val orderedEpisodes = remember(chronologicalEpisodes, isReversedOrder) {
        if (isReversedOrder) chronologicalEpisodes.reversed() else chronologicalEpisodes
    }

    // Calculate chunks
    val totalChunks = (totalEpisodeCount + EPISODE_CHUNK_SIZE - 1) / EPISODE_CHUNK_SIZE

    // Default chunk selection based on selectedChannelId
    val defaultChunkIndex = remember(detail.selectedChannelId, orderedEpisodes) {
        val selectedIdx = orderedEpisodes.indexOfFirst { it.episode.channelId == detail.selectedChannelId }
        if (selectedIdx >= 0) {
            (selectedIdx / EPISODE_CHUNK_SIZE).coerceIn(0, (totalChunks - 1).coerceAtLeast(0))
        } else {
            0
        }
    }

    var selectedChunkIndex by remember(isReversedOrder, detail.selectedChannelId) {
        mutableIntStateOf(defaultChunkIndex)
    }

    // Ensure selectedChunkIndex is valid
    val safeChunkIndex = selectedChunkIndex.coerceIn(0, (totalChunks - 1).coerceAtLeast(0))

    // Chunk list infos for the range tabs
    val chunkInfos = remember(orderedEpisodes, totalChunks, isReversedOrder, detail.selectedChannelId) {
        (0 until totalChunks).map { cIdx ->
            val startIdx = cIdx * EPISODE_CHUNK_SIZE
            val endIdx = minOf(startIdx + EPISODE_CHUNK_SIZE, orderedEpisodes.size)
            val chunkItems = orderedEpisodes.subList(startIdx, endIdx)
            val firstNum = chunkItems.first().episodeNumber
            val lastNum = chunkItems.last().episodeNumber
            val label = "$firstNum - $lastNum"
            val hasSelected = chunkItems.any { it.episode.channelId == detail.selectedChannelId }
            EpisodeChunkInfo(
                chunkIndex = cIdx,
                startEpisodeNum = firstNum,
                endEpisodeNum = lastNum,
                label = label,
                hasCurrentSelected = hasSelected
            )
        }
    }

    // Filtered results if searching
    val trimmedQuery = searchQuery.trim().lowercase()
    val isSearching = trimmedQuery.isNotBlank()

    val visibleEpisodes = remember(orderedEpisodes, isSearching, trimmedQuery, safeChunkIndex) {
        if (isSearching) {
            orderedEpisodes.filter { item ->
                val epNum = item.episodeNumber.toString()
                val title = item.episode.title.lowercase()
                epNum == trimmedQuery ||
                epNum.contains(trimmedQuery) ||
                title.contains(trimmedQuery) ||
                "eps $epNum".contains(trimmedQuery) ||
                "episode $epNum".contains(trimmedQuery)
            }
        } else if (totalChunks > 1) {
            val startIdx = safeChunkIndex * EPISODE_CHUNK_SIZE
            val endIdx = minOf(startIdx + EPISODE_CHUNK_SIZE, orderedEpisodes.size)
            orderedEpisodes.subList(startIdx, endIdx)
        } else {
            orderedEpisodes
        }
    }

    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val cardBorder = Color(0xFF222538)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Section: Title & Segmented View Switcher ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(title = "Daftar Episode")
                Text(
                    text = "($totalEpisodeCount)",
                    style = MaterialTheme.typography.titleMedium,
                    color = textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Clean Segmented View Mode Switcher: [ ☰ List | ⊞ Grid ]
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF141624),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // List Mode Tab
                    Surface(
                        onClick = { isGridView = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isGridView) accentIndigo else Color.Transparent,
                        modifier = Modifier.size(width = 34.dp, height = 28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ViewList,
                                contentDescription = "Tampilan List",
                                tint = if (!isGridView) Color.White else textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Grid Mode Tab
                    Surface(
                        onClick = { isGridView = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGridView) accentIndigo else Color.Transparent,
                        modifier = Modifier.size(width = 34.dp, height = 28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = "Tampilan Grid",
                                tint = if (isGridView) Color.White else textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Sub-Bar: Sort Pill, Search Pill, & Scrollable Range Tabs ---
        if (totalEpisodeCount > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sort Order Pill Button
                Surface(
                    onClick = { isReversedOrder = !isReversedOrder },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isReversedOrder) accentIndigo.copy(alpha = 0.20f) else Color(0xFF141624),
                    border = BorderStroke(1.dp, if (isReversedOrder) accentIndigo.copy(alpha = 0.50f) else cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = "Urutan Episode",
                            tint = if (isReversedOrder) accentSoft else textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isReversedOrder) "Terbaru" else "Terlama",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isReversedOrder) accentSoft else textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Search Toggle Pill Button
                Surface(
                    onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) {
                            searchQuery = ""
                            focusManager.clearFocus()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSearchActive || isSearching) accentIndigo.copy(alpha = 0.20f) else Color(0xFF141624),
                    border = BorderStroke(1.dp, if (isSearchActive || isSearching) accentIndigo.copy(alpha = 0.50f) else cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Cari",
                            tint = if (isSearchActive || isSearching) accentSoft else textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSearching) "Filter" else "Cari",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSearchActive || isSearching) accentSoft else textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Scrollable Range Tabs (if multiple chunks & not actively searching)
                if (totalChunks > 1 && !isSearching) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(chunkInfos, key = { it.chunkIndex }) { chunk ->
                            val isSelectedTab = chunk.chunkIndex == safeChunkIndex
                            Surface(
                                onClick = { selectedChunkIndex = chunk.chunkIndex },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelectedTab) accentIndigo else Color(0xFF141624),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelectedTab) accentIndigo else cardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (chunk.hasCurrentSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelectedTab) Color.White else Color(0xFF10B981),
                                            modifier = Modifier.size(5.dp)
                                        ) {}
                                    }
                                    Text(
                                        text = chunk.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelectedTab) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelectedTab) Color.White else textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Search Input Box (Clean Animated Expand) ---
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF141624),
                border = BorderStroke(1.dp, if (isSearching) accentIndigo.copy(alpha = 0.60f) else cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = accentSoft,
                        modifier = Modifier.size(17.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(accentSoft),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Ketik nomor episode (misal: 1050) atau judul...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary.copy(alpha = 0.70f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (searchQuery.isNotEmpty()) {
                        Surface(
                            onClick = { searchQuery = "" },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Hapus",
                                    tint = textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Search Results Info (when searching) ---
        if (isSearching) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ditemukan ${visibleEpisodes.size} episode",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentSoft,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = {
                        searchQuery = ""
                        isSearchActive = false
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Reset pencarian",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary
                    )
                }
            }
        }

        // --- Episodes Content (Empty, Responsive 5-Column Grid, or Detailed List) ---
        if (visibleEpisodes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141624),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Episode \"$searchQuery\" tidak ditemukan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Pastikan nomor episode atau kata kunci sudah benar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (isGridView) {
            // Pixel-Perfect 5-Column Weighted Grid
            val chunkedRows = remember(visibleEpisodes) { visibleEpisodes.chunked(5) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunkedRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val isSelected = item.episode.channelId == detail.selectedChannelId
                            EpisodeGridButton(
                                episodeNumber = item.episodeNumber,
                                isSelected = isSelected,
                                isFhd = item.episode.isFhdAvailable,
                                isHd = item.episode.isHdAvailable,
                                onClick = { onEpisodeClick(item.episode) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Symmetrical filler for incomplete rows
                        repeat(5 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Detailed List View
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                visibleEpisodes.forEach { item ->
                    EpisodeCard(
                        episode = item.episode,
                        episodeNumber = item.episodeNumber,
                        isSelected = item.episode.channelId == detail.selectedChannelId,
                        imageUrl = detail.imageUrl,
                        onClick = { onEpisodeClick(item.episode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeGridButton(
    episodeNumber: Int,
    isSelected: Boolean,
    isFhd: Boolean,
    isHd: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentIndigo = Color(0xFF6366F1)
    val cardBg = Color(0xFF141624)
    val cardBorder = Color(0xFF222538)
    val textPrimary = Color(0xFFF1F5F9)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentIndigo else cardBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) accentIndigo.copy(alpha = 0.85f) else cardBorder
        ),
        modifier = modifier.height(46.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "$episodeNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else textPrimary,
                    fontSize = 13.sp
                )
            }

            // Quality indicator dot (Top-Right)
            if (isFhd || isHd) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.90f) else Color(0xFF10B981),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 5.dp)
                        .size(4.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: AnimeEpisode,
    episodeNumber: Int,
    isSelected: Boolean,
    imageUrl: String,
    onClick: () -> Unit
) {
    val cardBg = Color(0xFF141624)
    val accentIndigo = Color(0xFF6366F1)
    val emeraldColor = Color(0xFF10B981)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentIndigo.copy(alpha = 0.15f) else cardBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) accentIndigo.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.07f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFF1A1D2C))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.50f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFFA5B4FC) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .background(SurfaceHighest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.40f)
                                .height(3.5.dp)
                                .background(accentIndigo)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = buildEpisodeLabel(episodeNumber = episodeNumber, isSelected = isSelected),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color(0xFFA5B4FC) else accentIndigo,
                        fontWeight = FontWeight.Bold
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = emeraldColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.75.dp, emeraldColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "DITONTON",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = episode.title.ifBlank { "Episode $episodeNumber" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = buildEpisodeFooter(episode),
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

private fun buildHeroTitle(title: String) = buildAnnotatedString {
    val safeTitle = title.ifBlank { "Anime Detail" }
    val separatorIndex = safeTitle.indexOf(':')
    if (separatorIndex in 1 until safeTitle.lastIndex) {
        append(safeTitle.substring(0, separatorIndex + 1))
        append(" ")
        pushStyle(SpanStyle(color = Color(0xFF818CF8)))
        append(safeTitle.substring(separatorIndex + 1).trim())
        pop()
    } else {
        append(safeTitle)
    }
}

private fun buildSeasonLabel(detail: AnimeDetailUiModel): String {
    return if (detail.episodes.isNotEmpty()) "1 Season" else "Series"
}

private fun buildEpisodeCountLabel(detail: AnimeDetailUiModel): String {
    val count = detail.episodes.size
    return if (count > 0) "$count Episode" else "-"
}

private fun buildEpisodeLabel(episodeNumber: Int, isSelected: Boolean): String {
    return "EPS $episodeNumber"
}

private fun buildEpisodeFooter(episode: AnimeEpisode): String {
    val quality = buildList {
        if (episode.isFhdAvailable) add("1080p FHD")
        else if (episode.isHdAvailable) add("720p HD")
        else add("360p")
    }.joinToString("/")
    val views = "${episode.viewCount.ifBlank { "0" }} tayang"
    return "$quality • $views"
}
