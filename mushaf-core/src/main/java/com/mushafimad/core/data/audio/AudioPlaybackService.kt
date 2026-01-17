package com.mushafimad.core.data.audio

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.ChapterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Commands for custom actions
 */
private const val CUSTOM_COMMAND_LOAD_CHAPTER = "LOAD_CHAPTER"
private const val CUSTOM_COMMAND_CHANGE_RECITER = "CHANGE_RECITER"
private const val CUSTOM_COMMAND_SET_SPEED = "SET_SPEED"
private const val CUSTOM_COMMAND_TOGGLE_REPEAT = "TOGGLE_REPEAT"

private const val ARG_CHAPTER_NUMBER = "chapter_number"
private const val ARG_RECITER_ID = "reciter_id"
private const val ARG_SPEED = "speed"

/**
 * MediaSessionService for background audio playback
 *
 * This service enables:
 * - Background audio playback
 * - Lock screen controls
 * - Notification playback controls
 * - Bluetooth headset controls
 * - Android Auto integration
 */
@OptIn(UnstableApi::class)
class AudioPlaybackService : MediaSessionService() {

    private lateinit var chapterRepository: ChapterRepository
    private lateinit var reciterService: ReciterService

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentChapter: Int? = null
    private var currentReciterId: Int? = null
    private var isRepeatEnabled: Boolean = false

    override fun onCreate() {
        super.onCreate()
        MushafLibrary.logger.info("AudioPlaybackService: onCreate()")

        // Initialize dependencies from MushafLibrary
        chapterRepository = MushafLibrary.getChapterRepository()
        reciterService = com.mushafimad.core.internal.ServiceRegistry.getReciterService()

        // Initialize ExoPlayer with audio configuration
        player = ExoPlayer.Builder(this)
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

        // Add player listener
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        if (isRepeatEnabled) {
                            MushafLibrary.logger.info("Repeat enabled, restarting playback")
                            player.seekTo(0)
                            player.play()
                        }
                    }
                    else -> { /* Handle other states if needed */ }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                MushafLibrary.logger.error("Playback error: ${error.message}", error)
            }
        })

        // Create MediaSession
        val sessionBuilder = MediaSession.Builder(this, player)
            .setCallback(AudioSessionCallback())

        // Set session activity if available
        packageManager?.getLaunchIntentForPackage(packageName)?.let { intent ->
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            sessionBuilder.setSessionActivity(pendingIntent)
        }

        mediaSession = sessionBuilder.build()

        MushafLibrary.logger.info("MediaSession created successfully")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        MushafLibrary.logger.info("AudioPlaybackService: onDestroy()")

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        // Cancel all coroutines
        serviceScope.coroutineContext.cancel()

        super.onDestroy()
    }

    /**
     * MediaSession callback to handle custom commands
     */
    private inner class AudioSessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_COMMAND_LOAD_CHAPTER, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_CHANGE_RECITER, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_SET_SPEED, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_REPEAT, Bundle.EMPTY))
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return when (customCommand.customAction) {
                CUSTOM_COMMAND_LOAD_CHAPTER -> {
                    val chapterNumber = args.getInt(ARG_CHAPTER_NUMBER, -1)
                    val reciterId = args.getInt(ARG_RECITER_ID, -1)

                    if (chapterNumber > 0 && reciterId > 0) {
                        loadChapter(chapterNumber, reciterId)
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    } else {
                        Futures.immediateFuture(SessionResult(SessionError.ERROR_BAD_VALUE))
                    }
                }

                CUSTOM_COMMAND_CHANGE_RECITER -> {
                    val reciterId = args.getInt(ARG_RECITER_ID, -1)

                    if (reciterId > 0 && currentChapter != null) {
                        loadChapter(currentChapter!!, reciterId)
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    } else {
                        Futures.immediateFuture(SessionResult(SessionError.ERROR_BAD_VALUE))
                    }
                }

                CUSTOM_COMMAND_SET_SPEED -> {
                    val speed = args.getFloat(ARG_SPEED, 1.0f)
                    player.setPlaybackSpeed(speed)
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                CUSTOM_COMMAND_TOGGLE_REPEAT -> {
                    isRepeatEnabled = !isRepeatEnabled
                    val result = Bundle().apply {
                        putBoolean("repeat_enabled", isRepeatEnabled)
                    }
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, result))
                }

                else -> {
                    super.onCustomCommand(session, controller, customCommand, args)
                }
            }
        }
    }

    /**
     * Load and prepare audio for a specific chapter and reciter
     */
    private fun loadChapter(chapterNumber: Int, reciterId: Int) {
        serviceScope.launch {
            try {
                val reciter = reciterService.getReciterById(reciterId)
                if (reciter == null) {
                    MushafLibrary.logger.error("Reciter not found: $reciterId")
                    return@launch
                }

                val audioUrl = reciter.getAudioUrl(chapterNumber)
                MushafLibrary.logger.info("Configured player for chapter $chapterNumber with reciter $reciterId")
                MushafLibrary.logger.info("Loading audio: $audioUrl")

                currentChapter = chapterNumber
                currentReciterId = reciterId

                // Fetch chapter metadata for notification
                val chapter = chapterRepository.getChapter(chapterNumber)
                val chapterTitle = chapter?.arabicTitle ?: "سورة $chapterNumber"
                val reciterName = reciter.nameArabic

                // Create MediaItem with metadata for notification
                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(chapterTitle)
                    .setArtist(reciterName)
                    .setDisplayTitle(chapterTitle)
                    .setSubtitle(reciterName)
                    .setAlbumTitle("القرآن الكريم") // "The Holy Quran"
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaMetadata(mediaMetadata)
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                MushafLibrary.logger.info("Playing: $chapterTitle by $reciterName")
            } catch (e: Exception) {
                MushafLibrary.logger.error("Failed to load chapter", e)
            }
        }
    }
}
