package com.mushafimad.core.data.audio

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mushafimad.core.MushafLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Playback states for audio player
 */
enum class PlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * Audio player state containing playback information
 */
data class AudioPlayerState(
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val currentChapter: Int? = null,
    val currentReciterId: Int? = null,
    val isBuffering: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val errorMessage: String? = null
) {
    val progressPercentage: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()) * 100f
        } else 0f

    val remainingTimeMs: Long
        get() = (durationMs - currentPositionMs).coerceAtLeast(0)

    val isPlaying: Boolean
        get() = playbackState == PlaybackState.PLAYING
}

/**
 * Manager for MediaSession connections to AudioPlaybackService
 *
 * This class handles:
 * - Connecting to AudioPlaybackService via MediaController
 * - Sending playback commands (play, pause, stop)
 * - Sending custom commands (load chapter, change reciter, etc.)
 * - Exposing playback state via StateFlow
 */
@OptIn(UnstableApi::class)
internal class MediaSessionManager(
    private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentChapter: Int? = null
    private var currentReciterId: Int? = null

    /**
     * Initialize and connect to AudioPlaybackService
     */
    fun initialize() {
        if (controllerFuture != null) {
            MushafLibrary.logger.info("MediaSessionManager already initialized")
            return
        }

        MushafLibrary.logger.info("Initializing MediaSessionManager")

        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    _isConnected.value = true
                    setupPlayerListener()
                    startStatePolling()
                    MushafLibrary.logger.info("MediaController connected successfully")
                } catch (e: Exception) {
                    MushafLibrary.logger.error("Failed to connect MediaController", e)
                    _isConnected.value = false
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    /**
     * Setup listener for player state changes
     */
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerState()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                MushafLibrary.logger.error("Playback error: ${error.message}", error)
                _playerState.value = _playerState.value.copy(
                    playbackState = PlaybackState.ERROR,
                    errorMessage = error.message ?: "Unknown error"
                )
            }
        })
    }

    /**
     * Start polling for player state updates
     */
    private fun startStatePolling() {
        scope.launch {
            while (isActive) {
                updatePlayerState()
                delay(100) // Update every 100ms
            }
        }
    }

    /**
     * Update player state from MediaController
     */
    private fun updatePlayerState() {
        val controller = mediaController ?: return

        val playbackState = when {
            controller.isPlaying -> PlaybackState.PLAYING
            controller.currentPosition > 0 || controller.duration > 0 -> PlaybackState.PAUSED
            else -> PlaybackState.IDLE
        }

        _playerState.value = _playerState.value.copy(
            playbackState = playbackState,
            currentPositionMs = controller.currentPosition,
            durationMs = controller.duration.let { if (it < 0) 0 else it },
            currentChapter = currentChapter,
            currentReciterId = currentReciterId
        )
    }

    /**
     * Load and play a chapter with specified reciter
     */
    fun loadChapter(chapterNumber: Int, reciterId: Int, autoPlay: Boolean = true) {
        scope.launch {
            currentChapter = chapterNumber
            currentReciterId = reciterId

            val args = Bundle().apply {
                putInt("chapter_number", chapterNumber)
                putInt("reciter_id", reciterId)
            }

            mediaController?.sendCustomCommand(
                SessionCommand("LOAD_CHAPTER", args),
                args
            )

            if (autoPlay) {
                mediaController?.play()
            }

            MushafLibrary.logger.info("Loaded chapter $chapterNumber with reciter $reciterId")
        }
    }

    /**
     * Change reciter (keeps current chapter)
     */
    fun changeReciter(reciterId: Int) {
        scope.launch {
            val args = Bundle().apply {
                putInt("reciter_id", reciterId)
            }

            mediaController?.sendCustomCommand(
                SessionCommand("CHANGE_RECITER", args),
                args
            )

            MushafLibrary.logger.info("Changed reciter to $reciterId")
        }
    }

    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        scope.launch {
            val args = Bundle().apply {
                putFloat("speed", speed)
            }

            mediaController?.sendCustomCommand(
                SessionCommand("SET_SPEED", args),
                args
            )

            MushafLibrary.logger.info("Set playback speed to ${speed}x")
        }
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        scope.launch {
            mediaController?.sendCustomCommand(
                SessionCommand("TOGGLE_REPEAT", Bundle.EMPTY),
                Bundle.EMPTY
            )

            MushafLibrary.logger.info("Toggled repeat mode")
        }
    }

    /**
     * Play
     */
    fun play() {
        mediaController?.play()
        MushafLibrary.logger.info("Play")
    }

    /**
     * Pause
     */
    fun pause() {
        mediaController?.pause()
        MushafLibrary.logger.info("Pause")
    }

    /**
     * Stop
     */
    fun stop() {
        mediaController?.stop()
        MushafLibrary.logger.info("Stop")
    }

    /**
     * Seek to position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        MushafLibrary.logger.info("Seek to ${positionMs}ms")
    }

    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean {
        return mediaController?.isPlaying ?: false
    }

    /**
     * Get current position in milliseconds
     */
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    /**
     * Get duration in milliseconds
     */
    fun getDuration(): Long {
        return mediaController?.duration?.let { if (it < 0) 0 else it } ?: 0L
    }

    /**
     * Release resources
     */
    fun release() {
        MushafLibrary.logger.info("Releasing MediaSessionManager")

        mediaController?.release()
        mediaController = null

        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null

        _isConnected.value = false
    }
}
