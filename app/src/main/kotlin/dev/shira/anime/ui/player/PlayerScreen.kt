package dev.shira.anime.ui.player

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dev.shira.anime.data.remote.AppConfigManager
import dev.shira.anime.domain.model.AnimeEpisode
import dev.shira.anime.ui.theme.VoidBlack
import kotlinx.coroutines.delay

enum class PlayerAspectRatio(val title: String) {
    FIT("Pas Layar (Fit)"),
    ZOOM("Isi Penuh (Zoom)"),
    FILL("Regang (Stretch)")
}

private const val VIDEO_USER_AGENT = "Player Anime v26.9.5"
private val SPEED_OPTIONS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

@Composable
fun PlayerScreen(
    initialVideoUrl: String,
    initialPositionMs: Long,
    qualities: List<VideoQuality>,
    title: String,
    episodes: List<AnimeEpisode> = emptyList(),
    currentChannelId: Int = 0,
    onSelectEpisode: (AnimeEpisode) -> Unit = {},
    onProgressChanged: (positionMs: Long, durationMs: Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedQuality by remember(initialVideoUrl, qualities) {
        mutableStateOf(
            qualities.firstOrNull { it.url == initialVideoUrl }
                ?: qualities.lastOrNull()
                ?: VideoQuality(label = "Auto", url = initialVideoUrl)
        )
    }
    val videoUrl = selectedQuality.url

    var resumePositionMs by remember { mutableStateOf(0L) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var bufferedPositionMs by remember { mutableLongStateOf(0L) }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember(videoUrl) { mutableStateOf(true) }
    var isEnded by remember { mutableStateOf(false) }
    var errorMessage by remember(videoUrl) { mutableStateOf<String?>(null) }

    // Controls state
    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Dialogs state
    var showQualityDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showAspectDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }

    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var aspectRatioMode by remember { mutableStateOf(PlayerAspectRatio.FIT) }

    // Double-tap seek feedback
    var doubleTapSeekText by remember { mutableStateOf<String?>(null) }
    var doubleTapSeekIsForward by remember { mutableStateOf(true) }

    // Scrubbing state
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    BackHandler(onBack = onBack)

    val player = remember(videoUrl) {
        val httpDataSourceFactory = buildVideoHttpDataSourceFactory(context, videoUrl)
        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val startPositionMs = resumePositionMs.takeIf { it > 0L } ?: initialPositionMs
                setMediaItem(MediaItem.fromUri(videoUrl))
                if (startPositionMs > 0L) {
                    seekTo(startPositionMs)
                }
                setPlaybackSpeed(playbackSpeed)
                playWhenReady = true
                prepare()
            }
    }

    // Double tap feedback auto dismiss
    LaunchedEffect(doubleTapSeekText) {
        if (doubleTapSeekText != null) {
            delay(750)
            doubleTapSeekText = null
        }
    }

    // Auto hide controls timer (4.5 seconds)
    LaunchedEffect(controlsVisible, isPlaying, lastInteractionTime, isLocked) {
        if (controlsVisible && isPlaying && !isLocked) {
            delay(4500)
            controlsVisible = false
        }
    }

    // Progress update loop
    LaunchedEffect(player, isPlaying) {
        while (true) {
            if (!isScrubbing) {
                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                durationMs = player.duration.coerceAtLeast(0L)
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                val dur = durationMs.takeIf { it > 0L } ?: 0L
                onProgressChanged(currentPositionMs, dur)
            }
            delay(300)
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                isEnded = playbackState == Player.STATE_ENDED
                isPlaying = player.isPlaying
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = error.localizedMessage ?: "Video tidak dapat diputar."
            }
        }

        fun notifyProgress() {
            val dur = player.duration.takeIf { it > 0L } ?: 0L
            onProgressChanged(player.currentPosition, dur)
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> player.play()
                Lifecycle.Event.ON_PAUSE -> {
                    notifyProgress()
                    player.pause()
                }
                else -> Unit
            }
        }

        player.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            notifyProgress()
            player.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            player.release()
        }
    }

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!isLocked) {
                            controlsVisible = !controlsVisible
                        }
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onDoubleTap = { offset ->
                        if (!isLocked) {
                            lastInteractionTime = System.currentTimeMillis()
                            if (offset.x < screenWidthPx / 2) {
                                val newPos = (player.currentPosition - 10000L).coerceAtLeast(0L)
                                player.seekTo(newPos)
                                currentPositionMs = newPos
                                doubleTapSeekIsForward = false
                                doubleTapSeekText = "-10 Detik"
                            } else {
                                val totalDur = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
                                val newPos = (player.currentPosition + 10000L).coerceAtMost(totalDur)
                                player.seekTo(newPos)
                                currentPositionMs = newPos
                                doubleTapSeekIsForward = true
                                doubleTapSeekText = "+10 Detik"
                            }
                        }
                    }
                )
            }
    ) {
        // 1. AndroidView ExoPlayer View
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    this.player = player
                    useController = false
                    keepScreenOn = true
                    resizeMode = when (aspectRatioMode) {
                        PlayerAspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        PlayerAspectRatio.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        PlayerAspectRatio.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                playerView.player = player
                playerView.resizeMode = when (aspectRatioMode) {
                    PlayerAspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    PlayerAspectRatio.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    PlayerAspectRatio.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Double-Tap Quick Seek Feedback Animation
        AnimatedVisibility(
            visible = doubleTapSeekText != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(250)) + scaleOut(tween(250)),
            modifier = Modifier.align(
                if (doubleTapSeekIsForward) Alignment.CenterEnd else Alignment.CenterStart
            )
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (doubleTapSeekIsForward) Icons.Outlined.Forward10 else Icons.Outlined.Replay10,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = doubleTapSeekText.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 3. Screen Lock Floating Toggle (When locked, only this is shown)
        if (isLocked) {
            Surface(
                onClick = {
                    isLocked = false
                    controlsVisible = true
                    lastInteractionTime = System.currentTimeMillis()
                },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.70f),
                border = BorderStroke(1.5.dp, Color(0xFF6366F1)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(48.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Buka Kunci Layar",
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 4. Custom Compose Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible && !isLocked,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Gradient Scrim
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                // Top Bar
                PlayerCustomTopBar(
                    title = title,
                    selectedQuality = selectedQuality,
                    playbackSpeed = playbackSpeed,
                    aspectRatio = aspectRatioMode,
                    hasEpisodes = episodes.isNotEmpty(),
                    onBack = onBack,
                    onOpenEpisodes = {
                        showEpisodeDialog = true
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onOpenQuality = {
                        showQualityDialog = true
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onOpenSpeed = {
                        showSpeedDialog = true
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onOpenAspect = {
                        showAspectDialog = true
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onToggleLock = {
                        isLocked = true
                        controlsVisible = false
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                )

                // Center Controls (Refined, balanced compact size: 44dp rewind/forward, 56dp center play/pause)
                PlayerCenterControls(
                    isPlaying = isPlaying,
                    isEnded = isEnded,
                    onPlayPause = {
                        if (isEnded) {
                            player.seekTo(0L)
                            player.play()
                        } else if (isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onRewind10 = {
                        val newPos = (player.currentPosition - 10000L).coerceAtLeast(0L)
                        player.seekTo(newPos)
                        currentPositionMs = newPos
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onForward10 = {
                        val totalDur = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
                        val newPos = (player.currentPosition + 10000L).coerceAtMost(totalDur)
                        player.seekTo(newPos)
                        currentPositionMs = newPos
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    modifier = Modifier.align(Alignment.Center)
                )

                // Bottom Bar (Seekbar, Time, Quick Skip OP)
                val displayPos = if (isScrubbing) scrubPositionMs else currentPositionMs
                PlayerCustomBottomBar(
                    currentPositionMs = displayPos,
                    durationMs = durationMs,
                    onSeek = { targetMs ->
                        isScrubbing = true
                        scrubPositionMs = targetMs
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onSeekFinished = {
                        player.seekTo(scrubPositionMs)
                        currentPositionMs = scrubPositionMs
                        isScrubbing = false
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onSkipIntro = {
                        val totalDur = player.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
                        val newPos = (player.currentPosition + 85000L).coerceAtMost(totalDur)
                        player.seekTo(newPos)
                        currentPositionMs = newPos
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }

        // 5. Buffering Indicator
        if (isBuffering && errorMessage == null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.70f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF818CF8),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Memuat video...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // 6. Error Dialog / Overlay
        errorMessage?.let { message ->
            PlayerErrorDialog(
                message = message,
                qualities = qualities,
                selectedQuality = selectedQuality,
                onSelectQuality = { quality ->
                    resumePositionMs = player.currentPosition
                    selectedQuality = quality
                    errorMessage = null
                },
                onRetry = {
                    val retryPositionMs = player.currentPosition
                    errorMessage = null
                    isBuffering = true
                    player.setMediaItem(MediaItem.fromUri(videoUrl))
                    if (retryPositionMs > 0L) {
                        player.seekTo(retryPositionMs)
                    }
                    player.prepare()
                    player.play()
                },
                onBack = onBack
            )
        }

        // 7. Modal Sheets / Dialogs
        if (showEpisodeDialog) {
            EpisodeSelectorDialog(
                episodes = episodes,
                currentChannelId = currentChannelId,
                onSelect = { episode ->
                    onSelectEpisode(episode)
                    showEpisodeDialog = false
                    lastInteractionTime = System.currentTimeMillis()
                },
                onDismiss = { showEpisodeDialog = false }
            )
        }

        if (showQualityDialog) {
            QualitySelectorDialog(
                qualities = qualities,
                selectedQuality = selectedQuality,
                onSelect = { quality ->
                    if (quality.url != selectedQuality.url) {
                        resumePositionMs = player.currentPosition
                        selectedQuality = quality
                    }
                    showQualityDialog = false
                },
                onDismiss = { showQualityDialog = false }
            )
        }

        if (showSpeedDialog) {
            SpeedSelectorDialog(
                currentSpeed = playbackSpeed,
                onSelect = { speed ->
                    playbackSpeed = speed
                    player.setPlaybackSpeed(speed)
                    showSpeedDialog = false
                },
                onDismiss = { showSpeedDialog = false }
            )
        }

        if (showAspectDialog) {
            AspectRatioDialog(
                currentAspect = aspectRatioMode,
                onSelect = { aspect ->
                    aspectRatioMode = aspect
                    showAspectDialog = false
                },
                onDismiss = { showAspectDialog = false }
            )
        }
    }
}

@Composable
private fun PlayerCustomTopBar(
    title: String,
    selectedQuality: VideoQuality,
    playbackSpeed: Float,
    aspectRatio: PlayerAspectRatio,
    hasEpisodes: Boolean,
    onBack: () -> Unit,
    onOpenEpisodes: () -> Unit,
    onOpenQuality: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenAspect: () -> Unit,
    onToggleLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentSoft = Color(0xFF818CF8)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.ifBlank { "Memutar Episode Anime" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Shira Player Engine • ExoPlayer Media3",
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Top Action Pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Episode List Button
            if (hasEpisodes) {
                PlayerTopPillButton(
                    icon = Icons.Outlined.VideoLibrary,
                    label = "Episode",
                    highlight = false,
                    onClick = onOpenEpisodes
                )
            }

            // Speed Pill
            PlayerTopPillButton(
                icon = Icons.Outlined.Speed,
                label = if (playbackSpeed == 1.0f) "1.0x" else "${playbackSpeed}x",
                onClick = onOpenSpeed
            )

            // Aspect Ratio Pill
            PlayerTopPillButton(
                icon = Icons.Outlined.AspectRatio,
                label = when (aspectRatio) {
                    PlayerAspectRatio.FIT -> "Fit"
                    PlayerAspectRatio.ZOOM -> "Zoom"
                    PlayerAspectRatio.FILL -> "Fill"
                },
                onClick = onOpenAspect
            )

            // Quality Pill
            PlayerTopPillButton(
                icon = Icons.Outlined.Hd,
                label = selectedQuality.label,
                highlight = true,
                onClick = onOpenQuality
            )

            // Lock Screen Button
            Surface(
                onClick = onToggleLock,
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.LockOpen,
                        contentDescription = "Kunci Layar",
                        tint = accentSoft,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTopPillButton(
    icon: ImageVector,
    label: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val accentIndigo = Color(0xFF6366F1)
    val textPrimary = Color(0xFFF1F5F9)
    val accentSoft = Color(0xFF818CF8)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (highlight) accentIndigo.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            if (highlight) accentIndigo.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) Color(0xFFA5B4FC) else accentSoft,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PlayerCenterControls(
    isPlaying: Boolean,
    isEnded: Boolean,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentIndigo = Color(0xFF6366F1)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        // -10s Rewind (44.dp)
        Surface(
            onClick = onRewind10,
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Replay10,
                    contentDescription = "Mundur 10 detik",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Center Play / Pause / Replay Button (Refined balanced size: 56.dp)
        Surface(
            onClick = onPlayPause,
            shape = CircleShape,
            color = accentIndigo,
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
            modifier = Modifier
                .size(56.dp)
                .shadow(12.dp, CircleShape, spotColor = accentIndigo, ambientColor = accentIndigo)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when {
                        isEnded -> Icons.Filled.Replay
                        isPlaying -> Icons.Filled.Pause
                        else -> Icons.Filled.PlayArrow
                    },
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // +10s Forward (44.dp)
        Surface(
            onClick = onForward10,
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Forward10,
                    contentDescription = "Maju 10 detik",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerCustomBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onSeekFinished: () -> Unit,
    onSkipIntro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Quick Action Row (Skip OP + Info)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatTime(currentPositionMs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 13.sp
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }

            // Skip Intro / OP Button (+85s)
            Surface(
                onClick = onSkipIntro,
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.60f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FastForward,
                        contentDescription = null,
                        tint = accentSoft,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Lewati Intro (+85s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Custom Styled Slider
        val maxDuration = durationMs.coerceAtLeast(1L).toFloat()
        val currentProgress = currentPositionMs.coerceIn(0L, durationMs.coerceAtLeast(1L)).toFloat()

        Slider(
            value = currentProgress,
            onValueChange = { newPos ->
                onSeek(newPos.toLong())
            },
            onValueChangeFinished = onSeekFinished,
            valueRange = 0f..maxDuration,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFA5B4FC),
                activeTrackColor = accentIndigo,
                inactiveTrackColor = Color.White.copy(alpha = 0.20f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private const val PLAYER_EPISODE_CHUNK_SIZE = 50

private data class PlayerDisplayEpisode(
    val episode: AnimeEpisode,
    val episodeNumber: Int,
    val chronologicalIndex: Int
)

private data class PlayerEpisodeChunkInfo(
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
private fun EpisodeSelectorDialog(
    episodes: List<AnimeEpisode>,
    currentChannelId: Int,
    onSelect: (AnimeEpisode) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = Color(0xFF141624)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)
    val accentIndigo = Color(0xFF6366F1)
    val accentSoft = Color(0xFF818CF8)
    val emeraldColor = Color(0xFF10B981)

    val totalCount = episodes.size
    var isReversedOrder by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(totalCount > 30) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Detect if raw list from API is sorted Descending (e.g. latest episode at index 0)
    val isRawDescending = remember(episodes) {
        if (episodes.size <= 1) false
        else {
            val firstNum = parseEpisodeNumber(episodes.first().title)
            val lastNum = parseEpisodeNumber(episodes.last().title)
            if (firstNum != null && lastNum != null) {
                firstNum > lastNum
            } else {
                episodes.first().channelId > episodes.last().channelId
            }
        }
    }

    // Master list in true chronological order (Episode 1 -> Episode N)
    val chronologicalEpisodes = remember(episodes, isRawDescending) {
        val baseList = if (isRawDescending) episodes.reversed() else episodes
        baseList.mapIndexed { index, ep ->
            val epNum = parseEpisodeNumber(ep.title) ?: (index + 1)
            PlayerDisplayEpisode(
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

    val totalChunks = (totalCount + PLAYER_EPISODE_CHUNK_SIZE - 1) / PLAYER_EPISODE_CHUNK_SIZE

    val defaultChunkIndex = remember(currentChannelId, orderedEpisodes) {
        val selectedIdx = orderedEpisodes.indexOfFirst { it.episode.channelId == currentChannelId }
        if (selectedIdx >= 0) {
            (selectedIdx / PLAYER_EPISODE_CHUNK_SIZE).coerceIn(0, (totalChunks - 1).coerceAtLeast(0))
        } else {
            0
        }
    }

    var selectedChunkIndex by remember(isReversedOrder, currentChannelId) {
        mutableIntStateOf(defaultChunkIndex)
    }

    val safeChunkIndex = selectedChunkIndex.coerceIn(0, (totalChunks - 1).coerceAtLeast(0))

    val chunkInfos = remember(orderedEpisodes, totalChunks, isReversedOrder, currentChannelId) {
        (0 until totalChunks).map { cIdx ->
            val startIdx = cIdx * PLAYER_EPISODE_CHUNK_SIZE
            val endIdx = minOf(startIdx + PLAYER_EPISODE_CHUNK_SIZE, orderedEpisodes.size)
            val chunkItems = orderedEpisodes.subList(startIdx, endIdx)
            val firstNum = chunkItems.first().episodeNumber
            val lastNum = chunkItems.last().episodeNumber
            val label = "$firstNum - $lastNum"
            val hasSelected = chunkItems.any { it.episode.channelId == currentChannelId }
            PlayerEpisodeChunkInfo(
                chunkIndex = cIdx,
                startEpisodeNum = firstNum,
                endEpisodeNum = lastNum,
                label = label,
                hasCurrentSelected = hasSelected
            )
        }
    }

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
            val startIdx = safeChunkIndex * PLAYER_EPISODE_CHUNK_SIZE
            val endIdx = minOf(startIdx + PLAYER_EPISODE_CHUNK_SIZE, orderedEpisodes.size)
            orderedEpisodes.subList(startIdx, endIdx)
        } else {
            orderedEpisodes
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row: Title & Segmented Switcher
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
                            imageVector = Icons.Outlined.VideoLibrary,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "Episode ($totalCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // Segmented View Mode Switcher: [ ☰ List | ⊞ Grid ]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1C1F32),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Surface(
                                onClick = { isGridView = false },
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isGridView) accentIndigo else Color.Transparent,
                                modifier = Modifier.size(width = 30.dp, height = 24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ViewList,
                                        contentDescription = "List",
                                        tint = if (!isGridView) Color.White else textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { isGridView = true },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isGridView) accentIndigo else Color.Transparent,
                                modifier = Modifier.size(width = 30.dp, height = 24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.GridView,
                                        contentDescription = "Grid",
                                        tint = if (isGridView) Color.White else textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-Bar: Sort, Search, Range Tabs
                if (totalCount > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Sort Button
                        Surface(
                            onClick = { isReversedOrder = !isReversedOrder },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isReversedOrder) accentIndigo.copy(alpha = 0.25f) else Color(0xFF1C1F32),
                            border = BorderStroke(
                                1.dp,
                                if (isReversedOrder) accentIndigo.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SwapVert,
                                    contentDescription = null,
                                    tint = if (isReversedOrder) accentSoft else textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isReversedOrder) "Terbaru" else "Terlama",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isReversedOrder) accentSoft else textSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        // Search Button
                        Surface(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) {
                                    searchQuery = ""
                                    focusManager.clearFocus()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSearchActive || isSearching) accentIndigo.copy(alpha = 0.25f) else Color(0xFF1C1F32),
                            border = BorderStroke(
                                1.dp,
                                if (isSearchActive || isSearching) accentIndigo.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Cari",
                                    tint = if (isSearchActive || isSearching) accentSoft else textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isSearching) "Filter" else "Cari",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSearchActive || isSearching) accentSoft else textSecondary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        // Range selector tabs (when not searching and totalChunks > 1)
                        if (totalChunks > 1 && !isSearching) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(chunkInfos, key = { it.chunkIndex }) { chunk ->
                                    val isSelectedTab = chunk.chunkIndex == safeChunkIndex
                                    Surface(
                                        onClick = { selectedChunkIndex = chunk.chunkIndex },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelectedTab) accentIndigo else Color(0xFF1C1F32),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelectedTab) accentIndigo else Color.White.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (chunk.hasCurrentSelected) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelectedTab) Color.White else emeraldColor,
                                                    modifier = Modifier.size(4.dp)
                                                ) {}
                                            }
                                            Text(
                                                text = chunk.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelectedTab) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelectedTab) Color.White else textSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Inline Search Bar
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                    exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1C1F32),
                        border = BorderStroke(1.dp, if (isSearching) accentIndigo.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.10f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = accentSoft,
                                modifier = Modifier.size(15.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(accentSoft),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Ketik nomor episode...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary.copy(alpha = 0.70f),
                                            fontSize = 11.5.sp
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
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Hapus",
                                            tint = textSecondary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        text = {
            if (episodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada daftar episode lain.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            } else if (visibleEpisodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Episode \"$searchQuery\" tidak ditemukan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            } else if (isGridView) {
                // Pixel-Perfect 5-Column Weighted Grid in Dialog
                val chunkedRows = remember(visibleEpisodes) { visibleEpisodes.chunked(5) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(chunkedRows) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { item ->
                                val isCurrent = item.episode.channelId == currentChannelId
                                Surface(
                                    onClick = { onSelect(item.episode) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCurrent) accentIndigo else Color.White.copy(alpha = 0.05f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) accentIndigo.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.08f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                            Text(
                                                text = "${item.episodeNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isCurrent) Color.White else textPrimary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(5 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // Detailed List in Dialog
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(visibleEpisodes, key = { it.episode.channelId }) { item ->
                        val episode = item.episode
                        val isCurrent = episode.channelId == currentChannelId
                        Surface(
                            onClick = { onSelect(episode) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) accentIndigo.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) accentIndigo.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCurrent) emeraldColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isCurrent) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                                                contentDescription = null,
                                                tint = if (isCurrent) emeraldColor else textSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "EPS ${item.episodeNumber}: ${episode.title.ifBlank { "Episode ${item.episodeNumber}" }}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) Color(0xFFA5B4FC) else textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val qualityTag = if (episode.isFhdAvailable) "1080p FHD" else if (episode.isHdAvailable) "720p HD" else "360p"
                                        Text(
                                            text = "$qualityTag • ${episode.viewCount.ifBlank { "0" }} tayang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = emeraldColor.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, emeraldColor.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "SEDANG DIPUTAR",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6EE7B7),
                                            fontSize = 9.5.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup", color = textSecondary)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun QualitySelectorDialog(
    qualities: List<VideoQuality>,
    selectedQuality: VideoQuality,
    onSelect: (VideoQuality) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = Color(0xFF161826)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hd,
                    contentDescription = null,
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Pilih Kualitas Video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                qualities.forEach { quality ->
                    val isSelected = quality.url == selectedQuality.url
                    Surface(
                        onClick = { onSelect(quality) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentIndigo.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentIndigo.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = quality.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFA5B4FC) else textPrimary
                            )
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentIndigo,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup", color = textSecondary)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SpeedSelectorDialog(
    currentSpeed: Float,
    onSelect: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = Color(0xFF161826)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = null,
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Kecepatan Putar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SPEED_OPTIONS.forEach { speed ->
                    val isSelected = speed == currentSpeed
                    Surface(
                        onClick = { onSelect(speed) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentIndigo.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentIndigo.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFA5B4FC) else textPrimary
                            )
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentIndigo,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup", color = textSecondary)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AspectRatioDialog(
    currentAspect: PlayerAspectRatio,
    onSelect: (PlayerAspectRatio) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = Color(0xFF161826)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AspectRatio,
                    contentDescription = null,
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ukuran Layar Video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlayerAspectRatio.entries.forEach { aspect ->
                    val isSelected = aspect == currentAspect
                    Surface(
                        onClick = { onSelect(aspect) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentIndigo.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentIndigo.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = aspect.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFA5B4FC) else textPrimary
                            )
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accentIndigo,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup", color = textSecondary)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PlayerErrorDialog(
    message: String,
    qualities: List<VideoQuality>,
    selectedQuality: VideoQuality,
    onSelectQuality: (VideoQuality) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val cardBg = Color(0xFF161826)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val accentIndigo = Color(0xFF6366F1)

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Gagal Memutar Video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )

                if (qualities.size > 1) {
                    Text(
                        text = "Coba ganti ke kualitas lain:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        qualities.forEach { quality ->
                            val isSelected = quality.url == selectedQuality.url
                            Surface(
                                onClick = { onSelectQuality(quality) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) accentIndigo else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = quality.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentIndigo,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Coba Lagi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text(text = "Kembali", color = textSecondary)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun buildVideoHttpDataSourceFactory(context: Context, videoUrl: String): DefaultHttpDataSource.Factory {
    val headers = mutableMapOf(
        "Icy-MetaData" to "1",
        "Accept-Encoding" to "identity"
    )

    val host = Uri.parse(videoUrl).host.orEmpty()
    if (host.contains("whatbox") || host.contains("box.ca")) {
        headers["Authorization"] = AppConfigManager.getWhatboxAuth(context)
    }

    return DefaultHttpDataSource.Factory()
        .setUserAgent(VIDEO_USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(headers)
}

private fun formatTime(durationMs: Long): String {
    if (durationMs <= 0L) return "00:00"
    val totalSeconds = durationMs / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    val hours = minutes / 60L

    return if (hours > 0L) {
        String.format("%d:%02d:%02d", hours, minutes % 60L, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
