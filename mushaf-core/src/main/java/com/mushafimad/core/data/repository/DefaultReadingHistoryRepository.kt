package com.mushafimad.core.data.repository

import com.mushafimad.core.data.local.dao.ReadingHistoryDao
import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import com.mushafimad.core.data.local.entities.ReadingHistoryEntity
import com.mushafimad.core.domain.models.LastReadPosition
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReadingHistory
import com.mushafimad.core.domain.models.ReadingStats
import com.mushafimad.core.domain.repository.ReadingHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Default implementation of ReadingHistoryRepository using ReadingHistoryDao
 * Fully testable - DAO can be swapped with fake implementation
 */
internal class DefaultReadingHistoryRepository (
    private val readingHistoryDao: ReadingHistoryDao
) : ReadingHistoryRepository {

    override fun getLastReadPositionFlow(mushafType: MushafType): Flow<LastReadPosition?> {
        return readingHistoryDao.getLastReadPositionFlow(mushafType.name)
            .map { entity -> entity?.toDomain() }
    }

    override suspend fun getLastReadPosition(mushafType: MushafType): LastReadPosition? = withContext(Dispatchers.IO) {
        readingHistoryDao.getLastReadPosition(mushafType.name)?.toDomain()
    }

    override suspend fun updateLastReadPosition(
        mushafType: MushafType,
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        scrollPosition: Float
    ): Unit = withContext(Dispatchers.IO) {
        readingHistoryDao.updateLastReadPosition(
            mushafType = mushafType.name,
            pageNumber = pageNumber,
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun recordReadingSession(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        durationSeconds: Int,
        mushafType: MushafType
    ): Unit = withContext(Dispatchers.IO) {
        readingHistoryDao.insertHistory(
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            pageNumber = pageNumber,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            mushafType = mushafType.name
        )
    }

    override suspend fun getRecentHistory(limit: Int): List<ReadingHistory> = withContext(Dispatchers.IO) {
        readingHistoryDao.getRecentHistory(limit).map { it.toDomain() }
    }

    override suspend fun getHistoryForDateRange(startTimestamp: Long, endTimestamp: Long): List<ReadingHistory> = withContext(Dispatchers.IO) {
        readingHistoryDao.getHistoryForDateRange(startTimestamp, endTimestamp)
            .map { it.toDomain() }
    }

    override suspend fun getHistoryForChapter(chapterNumber: Int): List<ReadingHistory> = withContext(Dispatchers.IO) {
        readingHistoryDao.getHistoryForChapter(chapterNumber).map { it.toDomain() }
    }

    override suspend fun deleteHistoryOlderThan(timestamp: Long) = withContext(Dispatchers.IO) {
        readingHistoryDao.deleteHistoryOlderThan(timestamp)
    }

    override suspend fun deleteAllHistory() = withContext(Dispatchers.IO) {
        readingHistoryDao.deleteAllHistory()
    }

    override suspend fun getReadingStats(startTimestamp: Long?, endTimestamp: Long?): ReadingStats = withContext(Dispatchers.IO) {
        val history = if (startTimestamp != null && endTimestamp != null) {
            readingHistoryDao.getHistoryForDateRange(startTimestamp, endTimestamp)
        } else {
            readingHistoryDao.getAllHistory()
        }

        val totalTime = history.sumOf { it.durationSeconds.toLong() }
        val pages = history.map { it.pageNumber }.distinct().size
        val chapters = history.map { it.chapterNumber }.distinct().size
        val verses = history.map { "${it.chapterNumber}:${it.verseNumber}" }.distinct().size
        val mostRead = history.groupBy { it.chapterNumber }
            .maxByOrNull { it.value.size }?.key
        val currentStreak = calculateCurrentStreak(history)
        val longestStreak = calculateLongestStreak(history)
        val avgDaily = if (history.isNotEmpty()) {
            val days = history.map { TimeUnit.MILLISECONDS.toDays(it.timestamp) }.distinct().size
            ((totalTime / 60) / days.coerceAtLeast(1)).toInt()
        } else 0

        ReadingStats(
            totalReadingTimeSeconds = totalTime,
            totalPagesRead = pages,
            totalChaptersRead = chapters,
            totalVersesRead = verses,
            mostReadChapter = mostRead,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            averageDailyMinutes = avgDaily
        )
    }

    override suspend fun getTotalReadingTime(): Long = withContext(Dispatchers.IO) {
        readingHistoryDao.getAllHistory().sumOf { it.durationSeconds.toLong() }
    }

    override suspend fun getReadChapters(): List<Int> = withContext(Dispatchers.IO) {
        readingHistoryDao.getAllHistory()
            .map { it.chapterNumber }
            .distinct()
            .sorted()
    }

    override suspend fun getCurrentStreak(): Int = withContext(Dispatchers.IO) {
        val history = readingHistoryDao.getRecentHistory(1000)  // Get recent entries for streak calculation
        calculateCurrentStreak(history)
    }

    private fun calculateCurrentStreak(history: List<ReadingHistoryEntity>): Int {
        if (history.isEmpty()) return 0

        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val days = history.map { TimeUnit.MILLISECONDS.toDays(it.timestamp) }.distinct().sorted().reversed()

        if (days.isEmpty() || days.first() < today - 1) return 0

        var streak = 0
        var expectedDay = today
        for (day in days) {
            if (day == expectedDay || day == expectedDay - 1) {
                streak++
                expectedDay = day - 1
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(history: List<ReadingHistoryEntity>): Int {
        if (history.isEmpty()) return 0

        val days = history.map { TimeUnit.MILLISECONDS.toDays(it.timestamp) }.distinct().sorted()
        var longest = 0
        var current = 1

        for (i in 1 until days.size) {
            if (days[i] == days[i - 1] + 1) {
                current++
            } else {
                longest = maxOf(longest, current)
                current = 1
            }
        }
        return maxOf(longest, current)
    }

    /**
     * Parse a persisted mushaf type without throwing.
     *
     * 0.2.1 wrote reading-history rows without a mushaf type at all, so any
     * database written by it holds rows with an empty string here. Reading one
     * back with MushafType.valueOf("") threw, and since 0.2.1 also started
     * recording sessions automatically, that took down every consumer that
     * showed reading history. Rows that predate the fix now fall back to the
     * default rather than crashing the app.
     */
    private fun parseMushafType(stored: String): MushafType =
        MushafType.entries.firstOrNull { it.name == stored } ?: MushafType.HAFS_1441

    private fun LastReadPositionEntity.toDomain() = LastReadPosition(
        mushafType = parseMushafType(mushafType),
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        pageNumber = pageNumber,
        lastReadAt = lastReadAt,
        scrollPosition = scrollPosition
    )

    private fun ReadingHistoryEntity.toDomain() = ReadingHistory(
        id = id.toHexString(),
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        pageNumber = pageNumber,
        timestamp = timestamp,
        durationSeconds = durationSeconds,
        mushafType = parseMushafType(mushafType)
    )
}
