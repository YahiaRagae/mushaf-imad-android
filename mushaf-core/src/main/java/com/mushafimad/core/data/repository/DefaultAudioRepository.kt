package com.mushafimad.core.data.repository

import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.AyahTimingService
import com.mushafimad.core.data.audio.MediaSessionManager
import com.mushafimad.core.data.audio.ReciterDataProvider
import com.mushafimad.core.data.audio.ReciterService
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of AudioRepository
 * Internal implementation - not exposed in public API
 */
internal class DefaultAudioRepository (
    private val mediaSessionManager: MediaSessionManager,
    private val ayahTimingService: AyahTimingService,
    private val reciterService: ReciterService
) : AudioRepository {


    init {
        // Initialize MediaSessionManager
        mediaSessionManager.initialize()
    }

    // Reciter operations
    override suspend fun getAllReciters(): List<ReciterInfo> {
        reciterService.awaitInitialization()
        return reciterService.availableReciters.value
    }

    override suspend fun getReciterById(reciterId: Int): ReciterInfo? {
        return reciterService.getReciterById(reciterId)
    }

    override suspend fun searchReciters(query: String, languageCode: String): List<ReciterInfo> {
        return reciterService.searchReciters(query, languageCode)
    }

    override suspend fun getHafsReciters(): List<ReciterInfo> {
        return reciterService.getHafsReciters()
    }

    override suspend fun getDefaultReciter(): ReciterInfo {
        reciterService.awaitInitialization()
        return reciterService.selectedReciter.value
            ?: reciterService.availableReciters.value.firstOrNull()
            ?: ReciterDataProvider.getDefaultReciter()
    }

    override fun saveSelectedReciter(reciter: ReciterInfo) {
        reciterService.selectReciter(reciter)
    }

    override fun getSelectedReciterFlow(): Flow<ReciterInfo?> {
        return reciterService.selectedReciter
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
