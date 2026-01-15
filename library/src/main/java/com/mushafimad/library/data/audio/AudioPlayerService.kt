package com.mushafimad.library.data.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mushafimad.library.MushafLibrary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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
    /**
     * Progress as a percentage (0-100)
     */
    val progressPercentage: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()) * 100f
        } else 0f

    /**
     * Remaining time in milliseconds
     */
    val remainingTimeMs: Long
        get() = (durationMs - currentPositionMs).coerceAtLeast(0)

    /**
     * Check if player is actively playing
     */
    val isPlaying: Boolean
        get() = playbackState == PlaybackState.PLAYING
}

/**
 * Service for managing audio playback using Media3 ExoPlayer
 * Internal implementation - not exposed in public API
 */
@Singleton
internal class AudioPlayerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        .build()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var currentChapter: Int? = null
    private var currentReciterId: Int? = null
    private var isRepeatEnabled: Boolean = false

    init {
        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> updateState(PlaybackState.IDLE)
                    Player.STATE_BUFFERING -> updateState(PlaybackState.LOADING, isBuffering = true)
                    Player.STATE_READY -> {
                        val newState = if (player.isPlaying) {
                            PlaybackState.PLAYING
                        } else {
                            PlaybackState.PAUSED
                        }
                        updateState(newState, isBuffering = false)
                    }
                    Player.STATE_ENDED -> {
                        updateState(PlaybackState.STOPPED)
                        onPlaybackEnded()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                MushafLibrary.logger.error("Playback error: ${error.message}", error)
                updateState(
                    PlaybackState.ERROR,
                    errorMessage = error.message ?: "Unknown playback error"
                )
            }
        })
    }

    /**
     * Load and prepare audio for a specific chapter and reciter
     * @param chapterNumber The chapter (surah) number (1-114)
     * @param reciterId The reciter ID
     * @param autoPlay Whether to start playback immediately
     */
    fun loadChapter(chapterNumber: Int, reciterId: Int, autoPlay: Boolean = false) {
        try {
            val reciter = ReciterDataProvider.getReciterById(reciterId)
            if (reciter == null) {
                MushafLibrary.logger.error("Reciter not found: $reciterId")
                updateState(PlaybackState.ERROR, errorMessage = "Reciter not found")
                return
            }

            val audioUrl = reciter.getAudioUrl(chapterNumber)
            MushafLibrary.logger.info("Loading audio: $audioUrl")

            currentChapter = chapterNumber
            currentReciterId = reciterId

            val mediaItem = MediaItem.fromUri(audioUrl)
            player.setMediaItem(mediaItem)
            player.prepare()

            updateState(PlaybackState.LOADING)

            if (autoPlay) {
                play()
            }
        } catch (e: Exception) {
            MushafLibrary.logger.error("Failed to load chapter", e)
            updateState(PlaybackState.ERROR, errorMessage = e.message ?: "Failed to load audio")
        }
    }

    /**
     * Start or resume playback
     */
    fun play() {
        player.play()
        updateState(PlaybackState.PLAYING)
    }

    /**
     * Pause playback
     */
    fun pause() {
        player.pause()
        updateState(PlaybackState.PAUSED)
    }

    /**
     * Stop playback and reset
     */
    fun stop() {
        player.stop()
        player.clearMediaItems()
        currentChapter = null
        currentReciterId = null
        updateState(PlaybackState.STOPPED)
    }

    /**
     * Seek to specific position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        updatePlayerPosition()
    }

    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    /**
     * Get total duration in milliseconds
     */
    fun getDuration(): Long {
        return player.duration.let { if (it < 0) 0 else it }
    }

    /**
     * Check if player is currently playing
     */
    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    /**
     * Set playback speed
     * @param speed Playback speed (0.5 = half speed, 1.0 = normal, 2.0 = double speed)
     */
    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    /**
     * Set repeat mode
     * @param enabled Whether to repeat the current chapter when it ends
     */
    fun setRepeatMode(enabled: Boolean) {
        isRepeatEnabled = enabled
        updateState(
            playbackState = _playerState.value.playbackState,
            isBuffering = _playerState.value.isBuffering,
            isRepeatEnabled = enabled,
            errorMessage = _playerState.value.errorMessage
        )
    }

    /**
     * Get current repeat mode
     */
    fun isRepeatEnabled(): Boolean = isRepeatEnabled

    /**
     * Release player resources
     */
    fun release() {
        stopPositionUpdates()
        player.release()
    }

    private fun updateState(
        playbackState: PlaybackState,
        isBuffering: Boolean = false,
        isRepeatEnabled: Boolean = this.isRepeatEnabled,
        errorMessage: String? = null
    ) {
        _playerState.value = AudioPlayerState(
            playbackState = playbackState,
            currentPositionMs = player.currentPosition,
            durationMs = getDuration(),
            currentChapter = currentChapter,
            currentReciterId = currentReciterId,
            isBuffering = isBuffering,
            isRepeatEnabled = isRepeatEnabled,
            errorMessage = errorMessage
        )
    }

    private fun updatePlayerPosition() {
        _playerState.value = _playerState.value.copy(
            currentPositionMs = player.currentPosition,
            durationMs = getDuration()
        )
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive && player.isPlaying) {
                updatePlayerPosition()
                delay(100) // Update every 100ms
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun onPlaybackEnded() {
        MushafLibrary.logger.info("Playback ended for chapter $currentChapter")

        if (isRepeatEnabled) {
            // Repeat current chapter
            MushafLibrary.logger.info("Repeat enabled, restarting chapter $currentChapter")
            seekTo(0)
            play()
        } else {
            stopPositionUpdates()
        }
    }
}
