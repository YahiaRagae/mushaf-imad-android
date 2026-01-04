package com.mushafimad.library.data.repository

import com.mushafimad.library.data.audio.AudioPlayerService
import com.mushafimad.library.data.audio.AudioPlayerState
import com.mushafimad.library.data.audio.AyahTimingService
import com.mushafimad.library.data.audio.ReciterDataProvider
import com.mushafimad.library.domain.models.AyahTiming
import com.mushafimad.library.domain.models.ReciterInfo
import com.mushafimad.library.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AudioRepository
 * Internal implementation - not exposed in public API
 */
@Singleton
internal class AudioRepositoryImpl @Inject constructor(
    private val audioPlayerService: AudioPlayerService,
    private val ayahTimingService: AyahTimingService
) : AudioRepository {

    // Reciter operations
    override fun getAllReciters(): List<ReciterInfo> {
        return ReciterDataProvider.allReciters
    }

    override fun getReciterById(reciterId: Int): ReciterInfo? {
        return ReciterDataProvider.getReciterById(reciterId)
    }

    override fun searchReciters(query: String, languageCode: String): List<ReciterInfo> {
        return ReciterDataProvider.searchReciters(query, languageCode)
    }

    override fun getHafsReciters(): List<ReciterInfo> {
        return ReciterDataProvider.getHafsReciters()
    }

    override fun getDefaultReciter(): ReciterInfo {
        return ReciterDataProvider.getDefaultReciter()
    }

    // Playback control
    override fun getPlayerStateFlow(): Flow<AudioPlayerState> {
        return audioPlayerService.playerState
    }

    override fun loadChapter(chapterNumber: Int, reciterId: Int, autoPlay: Boolean) {
        audioPlayerService.loadChapter(chapterNumber, reciterId, autoPlay)
    }

    override fun play() {
        audioPlayerService.play()
    }

    override fun pause() {
        audioPlayerService.pause()
    }

    override fun stop() {
        audioPlayerService.stop()
    }

    override fun seekTo(positionMs: Long) {
        audioPlayerService.seekTo(positionMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        audioPlayerService.setPlaybackSpeed(speed)
    }

    override fun getCurrentPosition(): Long {
        return audioPlayerService.getCurrentPosition()
    }

    override fun getDuration(): Long {
        return audioPlayerService.getDuration()
    }

    override fun isPlaying(): Boolean {
        return audioPlayerService.isPlaying()
    }

    // Timing operations
    override fun getAyahTiming(reciterId: Int, chapterNumber: Int, ayahNumber: Int): AyahTiming? {
        return ayahTimingService.getTiming(reciterId, chapterNumber, ayahNumber)
    }

    override fun getCurrentVerse(reciterId: Int, chapterNumber: Int, currentTimeMs: Int): Int? {
        return ayahTimingService.getCurrentVerse(reciterId, chapterNumber, currentTimeMs)
    }

    override fun getChapterTimings(reciterId: Int, chapterNumber: Int): List<AyahTiming> {
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
        audioPlayerService.release()
    }
}
