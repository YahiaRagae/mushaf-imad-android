package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.LastReadPosition
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.ReadingHistory
import com.mushafimad.library.domain.models.ReadingStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing reading history and progress
 * Public API - exposed to library consumers
 */
interface ReadingHistoryRepository {

    // Last Read Position

    /**
     * Observe the last read position for a specific mushaf type
     * @param mushafType The mushaf type
     */
    fun getLastReadPositionFlow(mushafType: MushafType): Flow<LastReadPosition?>

    /**
     * Get the last read position for a specific mushaf type
     * @param mushafType The mushaf type
     * @return LastReadPosition if found, null otherwise
     */
    suspend fun getLastReadPosition(mushafType: MushafType): LastReadPosition?

    /**
     * Update the last read position
     * @param mushafType The mushaf type
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     * @param pageNumber The page number
     * @param scrollPosition Optional scroll position (0-1)
     */
    suspend fun updateLastReadPosition(
        mushafType: MushafType,
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        scrollPosition: Float = 0f
    )

    // Reading History

    /**
     * Record a reading session
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     * @param pageNumber The page number
     * @param durationSeconds Duration of the reading session
     * @param mushafType The mushaf type
     */
    suspend fun recordReadingSession(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        durationSeconds: Int,
        mushafType: MushafType
    )

    /**
     * Get recent reading history
     * @param limit Maximum number of entries to return
     */
    suspend fun getRecentHistory(limit: Int = 50): List<ReadingHistory>

    /**
     * Get reading history for a specific date range
     * @param startTimestamp Start timestamp in milliseconds
     * @param endTimestamp End timestamp in milliseconds
     */
    suspend fun getHistoryForDateRange(startTimestamp: Long, endTimestamp: Long): List<ReadingHistory>

    /**
     * Get reading history for a specific chapter
     * @param chapterNumber The chapter number (1-114)
     */
    suspend fun getHistoryForChapter(chapterNumber: Int): List<ReadingHistory>

    /**
     * Delete reading history older than a specific timestamp
     * @param timestamp Timestamp in milliseconds
     */
    suspend fun deleteHistoryOlderThan(timestamp: Long)

    /**
     * Delete all reading history
     */
    suspend fun deleteAllHistory()

    // Reading Statistics

    /**
     * Get reading statistics
     * @param startTimestamp Optional start timestamp for statistics calculation
     * @param endTimestamp Optional end timestamp for statistics calculation
     */
    suspend fun getReadingStats(
        startTimestamp: Long? = null,
        endTimestamp: Long? = null
    ): ReadingStats

    /**
     * Get total reading time in seconds
     */
    suspend fun getTotalReadingTime(): Long

    /**
     * Get list of read chapters
     */
    suspend fun getReadChapters(): List<Int>

    /**
     * Get reading streak (consecutive days with reading activity)
     * @return Current streak in days
     */
    suspend fun getCurrentStreak(): Int
}
