package dev.shira.anime.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.shira.anime.R
import dev.shira.anime.data.local.ContinueWatchingItem
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.ui.common.AnimeSourceCard
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    state: HomeUiState,
    continueWatchingItem: ContinueWatchingItem?,
    currentSource: AnimeSourceType,
    onSelectSource: (AnimeSourceType) -> Unit = {},
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onSearchClick: () -> Unit,
    onGenreClick: () -> Unit,
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onContinueWatchingClick: (ContinueWatchingItem) -> Unit,
    onPostClick: (AnimePost) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentSource = currentSource,
                onGenreClick = {
                    coroutineScope.launch { drawerState.close() }
                    onGenreClick()
                },
                onSearchClick = {
                    coroutineScope.launch { drawerState.close() }
                    onSearchClick()
                },
                onHistoryClick = {
                    coroutineScope.launch { drawerState.close() }
                    onHistoryClick()
                },
                onSettingsClick = {
                    coroutineScope.launch { drawerState.close() }
                    onSettingsClick()
                }
            )
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VoidBlack)
        ) {
            when {
                state.isInitialLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                state.errorMessage != null -> MessageState(
                    title = "Gagal memuat home",
                    message = state.errorMessage,
                    actionText = "Coba lagi",
                    onAction = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
                state.posts.isEmpty() -> MessageState(
                    title = "Belum ada anime",
                    message = "Daftar anime terbaru masih kosong.",
                    actionText = "Muat ulang",
                    onAction = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
                else -> {
                    val featuredPost = state.posts.first()
                    val popularPosts = state.posts.drop(1)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
                            HeroSection(
                                post = featuredPost,
                                onWatchClick = { onPostClick(featuredPost) },
                                onSearchClick = onSearchClick,
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                        }

                    item {
                        GenreChipRail(onGenreClick = onGenreClick)
                    }

                    continueWatchingItem?.let { item ->
                        item {
                            ContinueWatchingSection(
                                item = item,
                                fallbackPost = featuredPost,
                                onContinueWatchingClick = onContinueWatchingClick,
                                onPostClick = onPostClick
                            )
                        }
                    }

                    item {
                        SectionTitle(
                            title = "Populer Hari Ini",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    items(
                        items = popularPosts.chunked(POPULAR_GRID_COLUMNS),
                        key = { row ->
                            row.joinToString("-") { post ->
                                val id = if (post.channelId != 0) post.channelId else post.categoryId
                                "${id}_${post.title}"
                            }
                        }
                    ) { rowPosts ->
                        PopularTodayRow(
                            posts = rowPosts,
                            onPostClick = onPostClick
                        )
                    }

                    if (state.canLoadMore || state.isLoadingMore) {
                        item {
                            LoadMoreSection(
                                isLoading = state.isLoadingMore,
                                onLoadMore = onLoadMore
                            )
                        }
                    }

                    state.loadMoreErrorMessage?.let { message ->
                        item {
                            BottomRetryRow(
                                message = message,
                                onRetry = onLoadMore
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
private fun LoadMoreSection(
    isLoading: Boolean,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onLoadMore,
            enabled = !isLoading,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = InkPanel,
                contentColor = MoonText,
                disabledContainerColor = InkPanel,
                disabledContentColor = MistText
            ),
            border = BorderStroke(1.dp, LineDark)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = VioletSignal,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Memuat...")
            } else {
                Text(text = "Load More...")
            }
        }
    }
}

@Composable
private fun HeroSection(
    post: AnimePost,
    onWatchClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(574.dp)
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = post.animeTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoidBlack.copy(alpha = 0.30f),
                            Color.Transparent,
                            VoidBlack.copy(alpha = 0.74f),
                            VoidBlack
                        )
                    )
                )
        )

        HomeTopAppBar(
            onSearchClick = onSearchClick,
            onMenuClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill(
                    text = if (post.isOngoing) "Simulcast" else "Tamat",
                    backgroundColor = SakuraPulse,
                    contentColor = Color(0xFF590026)
                )
                HeroPill(
                    text = "Episode Baru",
                    backgroundColor = InkPanel.copy(alpha = 0.58f),
                    contentColor = MoonText,
                    borderColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = post.animeTitle.ifBlank { post.title }.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MoonText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title.ifBlank { "Episode terbaru siap ditonton" },
                style = MaterialTheme.typography.bodyMedium,
                color = MistText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.88f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onWatchClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletSignal,
                        contentColor = Color(0xFF001F24)
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
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = InkPanel.copy(alpha = 0.58f),
                    contentColor = MoonText,
                    border = BorderStroke(1.dp, LineDark)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Tambah ke daftar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopAppBar(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            imageVector = Icons.Outlined.Menu,
            contentDescription = "Menu",
            onClick = onMenuClick
        )
        GlassIconButton(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Cari",
            onClick = onSearchClick
        )
    }
}

@Composable
private fun GlassIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = InkPanel.copy(alpha = 0.48f),
        contentColor = MistText,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Box(
            modifier = Modifier
                .size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
private fun HeroPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color = Color.Transparent
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun GenreChipRail(onGenreClick: () -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        items(HOME_CHIPS) { chip ->
            val selected = chip == HOME_CHIPS.first()
            Surface(
                onClick = { if (!selected) onGenreClick() },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) VioletSignal.copy(alpha = 0.10f) else InkPanel,
                contentColor = if (selected) VioletSignal else MoonText,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) VioletSignal.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.05f)
                )
            ) {
                Text(
                    text = chip,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    item: ContinueWatchingItem,
    fallbackPost: AnimePost,
    onContinueWatchingClick: (ContinueWatchingItem) -> Unit,
    onPostClick: (AnimePost) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(title = "Lanjutkan Menonton")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                ContinueWatchingCard(
                    item = item,
                    onClick = { onContinueWatchingClick(item) }
                )
            }

            items(CONTINUE_WATCHING_PREVIEW_COUNT) { _ ->
                ContinueWatchingPreviewCard(
                    post = fallbackPost,
                    onClick = { onPostClick(fallbackPost) }
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLowest)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.36f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = VioletSignal,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SurfaceHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.progressFraction.coerceIn(0.04f, 1f))
                        .height(4.dp)
                        .background(VioletSignal)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title.ifBlank { "Episode terakhir" },
            style = MaterialTheme.typography.labelMedium,
            color = MoonText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Lanjut dari ${formatDuration(item.positionMs)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MistText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContinueWatchingPreviewCard(
    post: AnimePost,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLowest)
        ) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = post.animeTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(SurfaceHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.32f)
                        .height(4.dp)
                        .background(VioletSignal)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.animeTitle.ifBlank { post.title },
            style = MaterialTheme.typography.labelMedium,
            color = MoonText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = post.title.ifBlank { "Episode terbaru" },
            style = MaterialTheme.typography.bodyMedium,
            color = MistText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PopularTodayRow(
    posts: List<AnimePost>,
    onPostClick: (AnimePost) -> Unit
) {
    when (posts.size) {
        1 -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            PopularPosterCard(
                post = posts.first(),
                onClick = { onPostClick(posts.first()) },
                modifier = Modifier.fillMaxWidth(0.32f)
            )
        }

        else -> Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            posts.forEach { post ->
                PopularPosterCard(
                    post = post,
                    onClick = { onPostClick(post) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PopularPosterCard(
    post: AnimePost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardKey = if (post.channelId != 0) post.channelId else post.categoryId
    var isVisible by remember(cardKey) { mutableStateOf(false) }

    LaunchedEffect(cardKey) {
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "popularPosterAlpha"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 14f,
        animationSpec = tween(durationMillis = 280),
        label = "popularPosterOffsetY"
    )
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .alpha(animatedAlpha)
            .offset {
                IntOffset(
                    x = 0,
                    y = with(density) { animatedOffsetY.dp.roundToPx() }
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .background(InkPanel)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = post.animeTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            VoidBlack.copy(alpha = 0.18f),
                            VoidBlack.copy(alpha = 0.86f)
                        )
                    )
                )
        )

        RatingBadge(
            ratingText = post.viewCount.ifBlank { "4.8" },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        if (!post.isOngoing) {
            StatusBadge(
                text = "Selesai",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = post.animeTitle.ifBlank { post.title },
                style = MaterialTheme.typography.labelMedium,
                color = MoonText,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildGenreCaption(post),
                style = MaterialTheme.typography.bodyMedium,
                color = VioletSignal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RatingBadge(
    ratingText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = InkPanel.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = AmberFrame,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = ratingText.take(4),
                style = MaterialTheme.typography.labelMedium,
                color = MoonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = ElevatedPanel.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MoonText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MoonText,
        modifier = modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun BottomLoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            color = VioletSignal,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Memuat anime lainnya...",
            style = MaterialTheme.typography.bodyMedium,
            color = MistText
        )
    }
}

@Composable
private fun BottomRetryRow(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MistText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = "Coba lagi")
        }
    }
}

private fun buildGenreCaption(post: AnimePost): String {
    val primary = post.language.ifBlank { "Anime" }
    val secondary = if (post.isOngoing) "Ongoing" else "Selesai"
    return "$primary • $secondary"
}

private const val CONTINUE_WATCHING_PREVIEW_COUNT = 2
private const val POPULAR_GRID_COLUMNS = 3
private val HOME_CHIPS = listOf("Semua", "Action", "Petualangan", "Sci-Fi", "Isekai", "Romansa")

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VioletSignal)
    }
}

@Composable
fun MessageState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MoonText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MistText
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onAction) {
            Text(text = actionText)
        }
    }
}

@Composable
private fun AppDrawerContent(
    currentSource: AnimeSourceType,
    onGenreClick: () -> Unit,
    onSearchClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)
    val emeraldStatus = Color(0xFF10B981)

    // Randomize background from available artwork
    val drawerBackgrounds = remember {
        listOf(R.drawable.bg, R.drawable.bg_2, R.drawable.bg_3)
    }
    val currentBgRes = remember { drawerBackgrounds.random() }

    // Enhanced glass panel styling for high readability on vivid backgrounds
    val glassPanelBg = Color(0xA00E101E)
    val glassBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0C0D14),
        drawerContentColor = textPrimary,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Full-bleed Background Anime Art with TopCenter alignment (natural proportion, no over-zoom)
            Image(
                painter = painterResource(id = currentBgRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Clear & Balanced Cinematic Gradient Scrim (artwork remains clear and visible)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x95080A12),  // ~58% alpha for status bar & top header
                            0.25f to Color(0x35080A12), // ~20% alpha (character art is vivid & clear)
                            0.65f to Color(0x50080A12), // ~30% alpha
                            1.0f to Color(0xAA080A12)   // ~65% alpha at footer
                        )
                    )
            )

            // 3. Subtle Vignette Side Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color(0x44000000),
                            0.5f to Color.Transparent,
                            1.0f to Color(0x33000000)
                        )
                    )
            )

            // 4. Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Premium Glass Brand Header
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = glassPanelBg,
                    border = glassBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x401E2236),
                                border = BorderStroke(1.5.dp, accentIndigo.copy(alpha = 0.55f))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.elaina_3),
                                    contentDescription = "Shira Anime",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "SHIRA ANIME",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accentIndigo.copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, accentIndigo.copy(alpha = 0.40f))
                                    ) {
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA5B4FC),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Streaming Anime & Donghua",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Active Server Status Indicator
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = emeraldStatus.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, emeraldStatus.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = emeraldStatus,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Text(
                                    text = "Server: ${currentSource.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6EE7B7)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Navigasi
                Text(
                    text = "NAVIGASI UTAMA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                DrawerMenuItem(
                    icon = Icons.Outlined.Search,
                    title = "Pencarian Anime",
                    subtitle = "Cari judul, episode & season",
                    onClick = onSearchClick
                )

                DrawerMenuItem(
                    icon = Icons.Rounded.Star,
                    title = "Kategori & Genre",
                    subtitle = "Action, Fantasy, Romance, Donghua...",
                    onClick = onGenreClick
                )

                DrawerMenuItem(
                    icon = Icons.Outlined.History,
                    title = "Riwayat Tontonan",
                    subtitle = "Lanjutkan episode yang belum selesai",
                    onClick = onHistoryClick
                )

                DrawerMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Pengaturan & Server",
                    subtitle = "Pilihan provider streaming & cache",
                    onClick = onSettingsClick
                )

                Spacer(modifier = Modifier.weight(1f))

                // Translucent Glass Footer
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = glassPanelBg,
                    border = glassBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ultra HD Streaming • Multi-Server",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Shira Anime v2.0 • Pro Edition",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val glassPanelBg = Color(0x70161826)
    val glassBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    val accentSoft = Color(0xFF818CF8)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = glassPanelBg,
        border = glassBorder,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x401E2236),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.30f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentSoft,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = textMuted,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}


