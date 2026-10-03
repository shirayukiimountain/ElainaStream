package dev.shira.anime

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import dev.shira.anime.data.local.ContinueWatchingItem
import dev.shira.anime.data.local.PlaybackProgressStore
import dev.shira.anime.data.local.SourcePreferences
import dev.shira.anime.data.remote.AppConfigManager
import dev.shira.anime.domain.model.AnimeCollectionItem
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.domain.model.AnimePost
import dev.shira.anime.domain.model.AnimeSourceType
import dev.shira.anime.ui.browse.GenreScreen
import dev.shira.anime.ui.browse.GenreViewModel
import dev.shira.anime.ui.browse.SearchScreen
import dev.shira.anime.ui.browse.SearchViewModel
import dev.shira.anime.ui.common.FloatingPillNavBar
import dev.shira.anime.ui.common.NavTab
import dev.shira.anime.ui.common.UiState
import dev.shira.anime.ui.detail.DetailScreen
import dev.shira.anime.ui.detail.DetailViewModel
import dev.shira.anime.ui.history.HistoryScreen
import dev.shira.anime.ui.home.HomeScreen
import dev.shira.anime.ui.home.HomeViewModel
import dev.shira.anime.ui.player.PlayerScreen
import dev.shira.anime.ui.player.VideoQuality
import dev.shira.anime.ui.settings.SettingsScreen
import dev.shira.anime.ui.theme.AnimeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory() }
    private val detailViewModel: DetailViewModel by viewModels { DetailViewModel.Factory() }
    private val searchViewModel: SearchViewModel by viewModels { SearchViewModel.Factory() }
    private val genreViewModel: GenreViewModel by viewModels { GenreViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SourcePreferences.init(applicationContext)
        lifecycleScope.launch {
            AppConfigManager.sync(applicationContext)
        }
        setContent {
            AnimeTheme {
                AnimeApp(
                    homeViewModel = homeViewModel,
                    detailViewModel = detailViewModel,
                    searchViewModel = searchViewModel,
                    genreViewModel = genreViewModel
                )
            }
        }
    }
}

@Composable
private fun AnimeApp(
    homeViewModel: HomeViewModel,
    detailViewModel: DetailViewModel,
    searchViewModel: SearchViewModel,
    genreViewModel: GenreViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val playbackProgressStore = remember(context) {
        PlaybackProgressStore(context.applicationContext)
    }
    val currentSource by SourcePreferences.currentSource.collectAsStateWithLifecycle()
    val homeState by homeViewModel.homeState.collectAsStateWithLifecycle()
    val detailState by detailViewModel.detailState.collectAsStateWithLifecycle()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val genreState by genreViewModel.uiState.collectAsStateWithLifecycle()
    var selectedPost by remember { mutableStateOf<AnimePost?>(null) }
    val screenStack = remember { mutableStateListOf<AnimeScreen>(AnimeScreen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: AnimeScreen.Home

    fun navigateTo(screen: AnimeScreen) {
        if (screenStack.lastOrNull() != screen) {
            screenStack.add(screen)
        }
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
        }
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    var playerUrl by remember { mutableStateOf("") }
    var playerTitle by remember { mutableStateOf("") }
    var playerImageUrl by remember { mutableStateOf("") }
    var playerChannelId by remember { mutableStateOf(0) }
    var playerCategoryId by remember { mutableStateOf(0) }
    var playerInitialPositionMs by remember { mutableStateOf(0L) }
    var playerQualities by remember { mutableStateOf(emptyList<VideoQuality>()) }
    var playerEpisodes by remember { mutableStateOf(emptyList<AnimeEpisode>()) }
    var continueWatchingItem by remember {
        mutableStateOf(playbackProgressStore.getLastContinueWatching())
    }
    var historyList by remember {
        mutableStateOf(playbackProgressStore.getAllHistory())
    }
    var autoPlayEpisode by remember { mutableStateOf(false) }

    LaunchedEffect(detailState, autoPlayEpisode, currentScreen) {
        if (autoPlayEpisode && currentScreen == AnimeScreen.Detail) {
            when (val state = detailState) {
                is UiState.Success -> {
                    val detail = state.data
                    if (detail.episodes.isNotEmpty()) {
                        playerEpisodes = detail.episodes
                    }
                    val qualities = detail.availableQualities
                    val bestUrl = detail.bestVideoUrl
                    if (bestUrl.isNotBlank() && qualities.isNotEmpty()) {
                        autoPlayEpisode = false
                        playerUrl = bestUrl
                        playerTitle = detail.episodeTitle
                        playerImageUrl = detail.imageUrl
                        playerChannelId = detail.selectedChannelId
                        playerCategoryId = detail.category?.id ?: selectedPost?.categoryId ?: 0
                        playerInitialPositionMs = playbackProgressStore.getPosition(playerChannelId)
                        playerQualities = qualities
                        navigateTo(AnimeScreen.Player)
                    } else {
                        autoPlayEpisode = false
                        Toast.makeText(context, "URL video belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
                is UiState.Error -> {
                    autoPlayEpisode = false
                }
                UiState.Loading -> Unit
            }
        }
    }

    val isTopLevel = currentScreen in listOf(
        AnimeScreen.Home,
        AnimeScreen.Search,
        AnimeScreen.History,
        AnimeScreen.Settings
    )

    val currentTab = when (currentScreen) {
        AnimeScreen.Home -> NavTab.Home
        AnimeScreen.Search -> NavTab.Search
        AnimeScreen.History -> NavTab.History
        AnimeScreen.Settings -> NavTab.Settings
        else -> null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AnimeScreen.Home -> HomeScreen(
                state = homeState,
                continueWatchingItem = continueWatchingItem,
                currentSource = currentSource,
                onSelectSource = { source ->
                    SourcePreferences.setSource(context, source)
                    Toast.makeText(context, "Source diganti ke: ${source.displayName}", Toast.LENGTH_SHORT).show()
                    homeViewModel.refresh()
                    genreViewModel.loadGenres()
                },
                onRefresh = homeViewModel::refresh,
                onLoadMore = homeViewModel::loadNextPage,
                onSearchClick = { navigateTo(AnimeScreen.Search) },
                onGenreClick = { navigateTo(AnimeScreen.Genre) },
                onHistoryClick = { navigateTo(AnimeScreen.History) },
                onSettingsClick = { navigateTo(AnimeScreen.Settings) },
                onContinueWatchingClick = { item ->
                    selectedPost = AnimePost(
                        channelId = item.channelId,
                        categoryId = item.categoryId,
                        title = item.title,
                        animeTitle = item.title,
                        imageUrl = item.imageUrl,
                        createdAt = "",
                        viewCount = "",
                        isOngoing = true,
                        isHdAvailable = false,
                        isFhdAvailable = false,
                        language = ""
                    )
                    detailViewModel.load(
                        channelId = item.channelId,
                        categoryId = item.categoryId,
                        fallbackTitle = item.title,
                        fallbackImageUrl = item.imageUrl
                    )
                    navigateTo(AnimeScreen.Detail)
                },
                onPostClick = { post ->
                    selectedPost = post
                    detailViewModel.load(
                        channelId = post.channelId,
                        categoryId = post.categoryId,
                        fallbackTitle = post.animeTitle.ifBlank { post.title },
                        fallbackImageUrl = post.imageUrl
                    )
                    navigateTo(AnimeScreen.Detail)
                }
            )

            AnimeScreen.Search -> SearchScreen(
                state = searchState,
                onBack = { navigateBack() },
                onQueryChanged = searchViewModel::onQueryChanged,
                onSearch = searchViewModel::search,
                onLoadMore = searchViewModel::loadNextPage,
                onItemClick = { item ->
                    selectedPost = item.toFallbackPost()
                    detailViewModel.load(
                        channelId = 0,
                        categoryId = item.id,
                        fallbackTitle = item.title,
                        fallbackImageUrl = item.imageUrl
                    )
                    navigateTo(AnimeScreen.Detail)
                }
            )

            AnimeScreen.History -> HistoryScreen(
                historyList = historyList,
                onItemClick = { item ->
                    selectedPost = AnimePost(
                        channelId = item.channelId,
                        categoryId = item.categoryId,
                        title = item.title,
                        animeTitle = item.title,
                        imageUrl = item.imageUrl,
                        createdAt = "",
                        viewCount = "",
                        isOngoing = true,
                        isHdAvailable = false,
                        isFhdAvailable = false,
                        language = ""
                    )
                    detailViewModel.load(
                        channelId = item.channelId,
                        categoryId = item.categoryId,
                        fallbackTitle = item.title,
                        fallbackImageUrl = item.imageUrl
                    )
                    navigateTo(AnimeScreen.Detail)
                },
                onDeleteItem = { channelId ->
                    playbackProgressStore.removeHistoryItem(channelId)
                    historyList = playbackProgressStore.getAllHistory()
                    continueWatchingItem = playbackProgressStore.getLastContinueWatching()
                },
                onClearAll = {
                    playbackProgressStore.clearHistory()
                    historyList = emptyList()
                    continueWatchingItem = null
                    Toast.makeText(context, "Semua riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                },
                onExploreClick = {
                    screenStack.clear()
                    screenStack.add(AnimeScreen.Home)
                }
            )

            AnimeScreen.Settings -> SettingsScreen(
                currentSource = currentSource,
                onSelectSource = { source ->
                    SourcePreferences.setSource(context, source)
                    Toast.makeText(context, "Source diganti ke: ${source.displayName}", Toast.LENGTH_SHORT).show()
                    homeViewModel.refresh()
                    genreViewModel.loadGenres()
                },
                onClearCache = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()
                    Toast.makeText(context, "Cache gambar berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                },
                onClearHistory = {
                    playbackProgressStore.clearHistory()
                    historyList = emptyList()
                    continueWatchingItem = null
                    Toast.makeText(context, "Riwayat tontonan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            )

            AnimeScreen.Genre -> GenreScreen(
                state = genreState,
                onBack = { navigateBack() },
                onRetryGenres = genreViewModel::loadGenres,
                onGenreSelected = genreViewModel::selectGenre,
                onItemClick = { item ->
                    selectedPost = item.toFallbackPost()
                    detailViewModel.load(
                        channelId = 0,
                        categoryId = item.id,
                        fallbackTitle = item.title,
                        fallbackImageUrl = item.imageUrl
                    )
                    navigateTo(AnimeScreen.Detail)
                }
            )

            AnimeScreen.Detail -> DetailScreen(
                state = detailState,
                onBack = { navigateBack() },
                onRetry = {
                    selectedPost?.let { post ->
                        detailViewModel.load(
                            channelId = post.channelId,
                            categoryId = post.categoryId,
                            fallbackTitle = post.animeTitle.ifBlank { post.title },
                            fallbackImageUrl = post.imageUrl
                        )
                    }
                },
                onEpisodeClick = { episode: AnimeEpisode ->
                    val post = selectedPost
                    val currentDetail = (detailState as? UiState.Success)?.data
                    currentDetail?.episodes?.let { if (it.isNotEmpty()) playerEpisodes = it }
                    if (currentDetail != null && currentDetail.selectedChannelId == episode.channelId && currentDetail.bestVideoUrl.isNotBlank() && currentDetail.availableQualities.isNotEmpty()) {
                        playerUrl = currentDetail.bestVideoUrl
                        playerTitle = currentDetail.episodeTitle
                        playerImageUrl = currentDetail.imageUrl
                        playerChannelId = currentDetail.selectedChannelId
                        playerCategoryId = currentDetail.category?.id ?: post?.categoryId ?: 0
                        playerInitialPositionMs = playbackProgressStore.getPosition(playerChannelId)
                        playerQualities = currentDetail.availableQualities
                        navigateTo(AnimeScreen.Player)
                    } else {
                        autoPlayEpisode = true
                        detailViewModel.load(
                            channelId = episode.channelId,
                            categoryId = episode.categoryId,
                            fallbackTitle = episode.title.ifBlank { post?.animeTitle.orEmpty() },
                            fallbackImageUrl = post?.imageUrl.orEmpty()
                        )
                    }
                },
                onWatchClick = { videoUrl ->
                    val detail = when (val state = detailState) {
                        is UiState.Success -> state.data
                        else -> null
                    }
                    val qualities = detail?.availableQualities.orEmpty()

                    if (videoUrl.isBlank() || qualities.isEmpty()) {
                        val firstEpisode = detail?.episodes?.firstOrNull()
                        if (firstEpisode != null) {
                            detail.episodes.let { if (it.isNotEmpty()) playerEpisodes = it }
                            autoPlayEpisode = true
                            detailViewModel.load(
                                channelId = firstEpisode.channelId,
                                categoryId = firstEpisode.categoryId,
                                fallbackTitle = firstEpisode.title.ifBlank { selectedPost?.animeTitle.orEmpty() },
                                fallbackImageUrl = selectedPost?.imageUrl.orEmpty()
                            )
                        } else {
                            Toast.makeText(context, "URL video belum tersedia", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        detail?.episodes?.let { if (it.isNotEmpty()) playerEpisodes = it }
                        playerUrl = videoUrl
                        playerTitle = detail?.episodeTitle ?: selectedPost?.title.orEmpty()
                        playerImageUrl = detail?.imageUrl ?: selectedPost?.imageUrl.orEmpty()
                        playerChannelId = detail?.selectedChannelId ?: selectedPost?.channelId ?: 0
                        playerCategoryId = detail?.category?.id ?: selectedPost?.categoryId ?: 0
                        playerInitialPositionMs = playbackProgressStore.getPosition(playerChannelId)
                        playerQualities = qualities
                        navigateTo(AnimeScreen.Player)
                    }
                }
            )

            AnimeScreen.Player -> {
                PlayerWindowEffect()
                LaunchedEffect(detailState) {
                    val detail = (detailState as? UiState.Success)?.data
                    if (detail != null) {
                        if (detail.episodes.isNotEmpty()) {
                            playerEpisodes = detail.episodes
                        }
                        val qualities = detail.availableQualities
                        val bestUrl = detail.bestVideoUrl
                        if (bestUrl.isNotBlank() && qualities.isNotEmpty() && detail.selectedChannelId != playerChannelId) {
                            playerUrl = bestUrl
                            playerTitle = detail.episodeTitle
                            playerImageUrl = detail.imageUrl
                            playerChannelId = detail.selectedChannelId
                            playerCategoryId = detail.category?.id ?: selectedPost?.categoryId ?: 0
                            playerInitialPositionMs = playbackProgressStore.getPosition(playerChannelId)
                            playerQualities = qualities
                        }
                    }
                }
                PlayerScreen(
                    initialVideoUrl = playerUrl,
                    initialPositionMs = playerInitialPositionMs,
                    qualities = playerQualities,
                    title = playerTitle,
                    episodes = playerEpisodes.ifEmpty { (detailState as? UiState.Success)?.data?.episodes.orEmpty() },
                    currentChannelId = playerChannelId,
                    onSelectEpisode = { episode ->
                        val post = selectedPost
                        detailViewModel.load(
                            channelId = episode.channelId,
                            categoryId = episode.categoryId,
                            fallbackTitle = episode.title.ifBlank { post?.animeTitle.orEmpty() },
                            fallbackImageUrl = post?.imageUrl.orEmpty()
                        )
                    },
                    onProgressChanged = { positionMs, durationMs ->
                        if (playerChannelId != 0 && playerCategoryId != 0) {
                            val item = ContinueWatchingItem(
                                channelId = playerChannelId,
                                categoryId = playerCategoryId,
                                title = playerTitle,
                                imageUrl = playerImageUrl,
                                positionMs = positionMs,
                                durationMs = durationMs,
                                updatedAtMs = System.currentTimeMillis()
                            )
                            coroutineScope.launch(Dispatchers.IO) {
                                playbackProgressStore.saveProgress(item)
                            }
                        }
                    },
                    onBack = {
                        continueWatchingItem = playbackProgressStore.getLastContinueWatching()
                        historyList = playbackProgressStore.getAllHistory()
                        navigateBack()
                    }
                )
            }
        }

        if (currentTab != null) {
            FloatingPillNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Home -> {
                            if (currentScreen != AnimeScreen.Home) {
                                screenStack.clear()
                                screenStack.add(AnimeScreen.Home)
                            }
                        }
                        NavTab.Search -> {
                            if (currentScreen != AnimeScreen.Search) {
                                if (isTopLevel && screenStack.isNotEmpty() && screenStack.last() != AnimeScreen.Home) {
                                    screenStack.removeAt(screenStack.size - 1)
                                }
                                screenStack.add(AnimeScreen.Search)
                            }
                        }
                        NavTab.History -> {
                            historyList = playbackProgressStore.getAllHistory()
                            if (currentScreen != AnimeScreen.History) {
                                if (isTopLevel && screenStack.isNotEmpty() && screenStack.last() != AnimeScreen.Home) {
                                    screenStack.removeAt(screenStack.size - 1)
                                }
                                screenStack.add(AnimeScreen.History)
                            }
                        }
                        NavTab.Settings -> {
                            if (currentScreen != AnimeScreen.Settings) {
                                if (isTopLevel && screenStack.isNotEmpty() && screenStack.last() != AnimeScreen.Home) {
                                    screenStack.removeAt(screenStack.size - 1)
                                }
                                screenStack.add(AnimeScreen.Settings)
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PlayerWindowEffect() {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val previousOrientation = activity?.requestedOrientation

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, view).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose {
            activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

private fun AnimeCollectionItem.toFallbackPost(): AnimePost {
    return AnimePost(
        channelId = 0,
        categoryId = id,
        title = title,
        animeTitle = title,
        imageUrl = imageUrl,
        createdAt = "",
        viewCount = totalViews.toString(),
        isOngoing = isOngoing,
        isHdAvailable = false,
        isFhdAvailable = false,
        language = language
    )
}

private enum class AnimeScreen {
    Home,
    Search,
    History,
    Settings,
    Genre,
    Detail,
    Player
}
