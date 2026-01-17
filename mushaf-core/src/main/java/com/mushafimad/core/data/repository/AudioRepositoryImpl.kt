package com.mushafimad.core.data.repository

import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.AyahTimingService
import com.mushafimad.core.data.audio.MediaSessionManager
import com.mushafimad.core.data.audio.ReciterDataProvider
import com.mushafimad.core.data.audio.ReciterService
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of AudioRepository
 * Internal implementation - not exposed in public API
 */
internal class AudioRepositoryImpl private constructor(
    private val mediaSessionManager: MediaSessionManager,
    private val ayahTimingService: AyahTimingService,
    private val reciterService: ReciterService
) : AudioRepository {

    companion object {
        @Volatile private var instance: AudioRepositoryImpl? = null

        fun getInstance(): AudioRepository = instance ?: synchronized(this) {
            instance ?: AudioRepositoryImpl(
                ServiceRegistry.getMediaSessionManager(),
                ServiceRegistry.getAyahTimingService(),
                ServiceRegistry.getReciterService()
            ).also { instance = it }
        }
    }

    init {
        // Initialize MediaSessionManager
        mediaSessionManager.initialize()
    }

    // Reciter operations
    override fun getAllReciters(): List<ReciterInfo> {
        return reciterService.availableReciters.value
    }

    override fun getReciterById(reciterId: Int): ReciterInfo? {
        return reciterService.getReciterById(reciterId)
    }

    override fun searchReciters(query: String, languageCode: String): List<ReciterInfo> {
        return reciterService.searchReciters(query, languageCode)
    }

    override fun getHafsReciters(): List<ReciterInfo> {
        return reciterService.getHafsReciters()
    }

    override fun getDefaultReciter(): ReciterInfo {
        return reciterService.selectedReciter.value
            ?: reciterService.availableReciters.value.firstOrNull()
            ?: ReciterDataProvider.getDefaultReciter()
    }

    // Playback control
    override fun getPlayerStateFlow(): Flow<AudioPlayerState> {
        return mediaSessionManager.playerState
    }

    override fun loadChapter(chapterNumber: Int, reciterId: Int, autoPlay: Boolean) {
        mediaSessionManager.loadChapter(chapterNumber, reciterId, autoPlay)
    }

    override fun play() {
        mediaSessionManager.play()
    }

    override fun pause() {
        mediaSessionManager.pause()
    }

    override fun stop() {
        mediaSessionManager.stop()
    }

    override fun seekTo(positionMs: Long) {
        mediaSessionManager.seekTo(positionMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        mediaSessionManager.setPlaybackSpeed(speed)
    }

    override fun setRepeatMode(enabled: Boolean) {
        // Note: MediaSessionManager uses toggleRepeat(), so we need to check current state
        val currentState = mediaSessionManager.playerState.value.isRepeatEnabled
        if (currentState != enabled) {
            mediaSessionManager.toggleRepeat()
        }
    }

    override fun isRepeatEnabled(): Boolean {
        return mediaSessionManager.playerState.value.isRepeatEnabled
    }

    override fun getCurrentPosition(): Long {
        return mediaSessionManager.getCurrentPosition()
    }

    override fun getDuration(): Long {
        return mediaSessionManager.getDuration()
    }

    override fun isPlaying(): Boolean {
        return mediaSessionManager.isPlaying()
    }

    // Timing operations
    override suspend fun getAyahTiming(reciterId: Int, chapterNumber: Int, ayahNumber: Int): AyahTiming? {
        return ayahTimingService.getTiming(reciterId, chapterNumber, ayahNumber)
    }

    override suspend fun getCurrentVerse(reciterId: Int, chapterNumber: Int, currentTimeMs: Int): Int? {
        return ayahTimingService.getCurrentVerse(reciterId, chapterNumber, currentTimeMs)
    }

    override suspend fun getChapterTimings(reciterId: Int, chapterNumber: Int): List<AyahTiming> {
        return ayahTimingService.getChapterTimings(reciterId, chapterNumber)
    }

    override fun hasTimingForReciter(reciterId: Int): Boolean {
        return ayahTimingService.hasTimingForReciter(reciterId)
    }

    override suspend fun preloadTiming(reciterId: Int) {
        ayahTimingService.preloadTiming(reciterId)
    }

    // Lifecycle
    override fun release() {
        mediaSessionManager.release()
    }
}
