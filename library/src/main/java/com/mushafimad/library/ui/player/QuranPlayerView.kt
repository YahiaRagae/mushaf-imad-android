package com.mushafimad.library.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushafimad.library.data.audio.PlaybackState
import com.mushafimad.library.domain.models.ReciterInfo
import com.mushafimad.library.ui.theme.MushafColors

/**
 * Quran audio player UI component
 * Displays playback controls, progress bar, and chapter information
 * Public API - exposed to library consumers
 *
 * @param chapterNumber Chapter number (1-114)
 * @param chapterName Chapter name for display
 * @param reciterId Optional reciter ID (uses default if not specified)
 * @param autoPlay Whether to start playing immediately when loaded
 * @param onPreviousVerse Callback for previous verse navigation
 * @param onNextVerse Callback for next verse navigation
 * @param modifier Optional modifier
 */
@Composable
fun QuranPlayerView(
    chapterNumber: Int,
    chapterName: String,
    reciterId: Int? = null,
    autoPlay: Boolean = true,
    onPreviousVerse: (() -> Unit)? = null,
    onNextVerse: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val viewModel: QuranPlayerViewModel = hiltViewModel()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playbackRate by viewModel.playbackRate.collectAsState()
    val isRepeatEnabled by viewModel.isRepeatEnabled.collectAsState()
    val currentVerseNumber by viewModel.currentVerseNumber.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val availableReciters by viewModel.availableReciters.collectAsState()

    var showReciterPicker by remember { mutableStateOf(false) }

    // Configure player on initial load
    LaunchedEffect(chapterNumber, reciterId) {
        viewModel.configure(chapterNumber, chapterName, reciterId)
        viewModel.loadChapter(autoPlay)
    }

    // Accent color matching iOS
    val accentColor = Color(0xFF2D7F6E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Reciter name, chapter name, verse number
            PlayerHeader(
                reciterName = selectedReciter?.getDisplayName() ?: "",
                chapterName = chapterName,
                currentVerseNumber = currentVerseNumber,
                playbackState = playbackState,
                onReciterClick = { showReciterPicker = true }
            )

            // Progress bar
            PlayerProgressBar(
                currentTimeMs = currentTimeMs,
                durationMs = durationMs,
                accentColor = accentColor,
                enabled = durationMs > 0 && playbackState != PlaybackState.LOADING,
                onSeek = { progress ->
                    val newPosition = (durationMs * progress).toLong()
                    viewModel.seekTo(newPosition)
                }
            )

            // Control buttons
            PlayerControls(
                playbackState = playbackState,
                isRepeatEnabled = isRepeatEnabled,
                playbackRate = playbackRate,
                accentColor = accentColor,
                onPlayPause = { viewModel.togglePlayback() },
                onPreviousVerse = onPreviousVerse,
                onNextVerse = onNextVerse,
                onToggleRepeat = { viewModel.toggleRepeat() },
                onCyclePlaybackRate = { viewModel.cyclePlaybackRate() }
            )
        }
    }

    // Reciter picker dialog
    if (showReciterPicker) {
        ReciterPickerDialog(
            reciters = availableReciters,
            selectedReciter = selectedReciter,
            onReciterSelected = { reciter ->
                viewModel.selectReciter(reciter, reloadAudio = true)
                showReciterPicker = false
            },
            onDismiss = { showReciterPicker = false }
        )
    }
}

/**
 * Player header displaying reciter, chapter name, and verse number
 */
@Composable
private fun PlayerHeader(
    reciterName: String,
    chapterName: String,
    currentVerseNumber: Int?,
    playbackState: PlaybackState,
    onReciterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Reciter name (clickable)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onReciterClick)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reciterName,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select reciter",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Chapter name and verse number
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chapterName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (currentVerseNumber != null && currentVerseNumber > 0) {
                Text(
                    text = ":",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currentVerseNumber.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Playback state indicator
        PlaybackStateIndicator(playbackState)
    }
}

/**
 * Playback state indicator (loading, buffering, paused, etc.)
 */
@Composable
private fun PlaybackStateIndicator(
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    when (playbackState) {
        PlaybackState.LOADING -> {
            StateRow("Loading audio...", showSpinner = true)
        }
        PlaybackState.PAUSED -> {
            StateRow("Paused", icon = Icons.Default.Pause)
        }
        PlaybackState.STOPPED -> {
            StateRow("Playback completed", icon = Icons.Default.CheckCircle)
        }
        PlaybackState.ERROR -> {
            StateRow("Error occurred", icon = Icons.Default.Error, isError = true)
        }
        else -> { /* No indicator for IDLE or PLAYING */ }
    }
}

@Composable
private fun StateRow(
    label: String,
    showSpinner: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isError: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isError) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Progress bar with time labels
 */
@Composable
private fun PlayerProgressBar(
    currentTimeMs: Long,
    durationMs: Long,
    accentColor: Color,
    enabled: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) currentTimeMs.toFloat() / durationMs.toFloat() else 0f
    val remainingMs = (durationMs - currentTimeMs).coerceAtLeast(0)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Progress bar
            var dragProgress by remember { mutableStateOf<Float?>(null) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .then(
                        if (enabled) {
                            Modifier.pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { offset ->
                                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                                    },
                                    onHorizontalDrag = { change, _ ->
                                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                    },
                                    onDragEnd = {
                                        dragProgress?.let { onSeek(it) }
                                        dragProgress = null
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(dragProgress ?: progress)
                        .background(accentColor)
                )
            }

            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentTimeMs),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )

                Text(
                    text = "-${formatTime(remainingMs)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
            }
        }
    }
}

/**
 * Control buttons: repeat, previous, play/pause, next, playback rate
 */
@Composable
private fun PlayerControls(
    playbackState: PlaybackState,
    isRepeatEnabled: Boolean,
    playbackRate: Float,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onPreviousVerse: (() -> Unit)?,
    onNextVerse: (() -> Unit)?,
    onToggleRepeat: () -> Unit,
    onCyclePlaybackRate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = playbackState == PlaybackState.LOADING
    val isPlaying = playbackState == PlaybackState.PLAYING

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repeat button
            IconButton(
                onClick = onToggleRepeat,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = if (isRepeatEnabled) Icons.Default.Repeat else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeatEnabled) accentColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous verse button
            IconButton(
                onClick = { onPreviousVerse?.invoke() },
                enabled = onPreviousVerse != null && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous verse",
                    modifier = Modifier.size(28.dp),
                    tint = if (onPreviousVerse != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }

            // Play/Pause button
            IconButton(
                onClick = onPlayPause,
                enabled = !isLoading || isPlaying,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Next verse button
            IconButton(
                onClick = { onNextVerse?.invoke() },
                enabled = onNextVerse != null && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next verse",
                    modifier = Modifier.size(28.dp),
                    tint = if (onNextVerse != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }

            // Playback rate button
            IconButton(
                onClick = onCyclePlaybackRate,
                enabled = !isLoading
            ) {
                Text(
                    text = formatPlaybackRate(playbackRate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Format time in milliseconds to MM:SS format
 */
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

/**
 * Format playback rate for display
 */
private fun formatPlaybackRate(rate: Float): String {
    return if (rate == 1.0f) "1x" else String.format("%.2fx", rate).replace(".00", "")
}
