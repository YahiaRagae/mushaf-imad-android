package com.mushafimad.core.data.repository

import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import com.mushafimad.core.data.local.entities.ReadingHistoryEntity
import com.mushafimad.core.domain.models.LastReadPosition
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReadingHistory
import com.mushafimad.core.domain.models.ReadingStats
import com.mushafimad.core.domain.repository.ReadingHistoryRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

internal class ReadingHistoryRepositoryImpl private constructor(
    private val realmService: RealmService
) : ReadingHistoryRepository {

    companion object {
        @Volatile private var instance: ReadingHistoryRepositoryImpl? = null

        fun getInstance(): ReadingHistoryRepository = instance ?: synchronized(this) {
            instance ?: ReadingHistoryRepositoryImpl(
                ServiceRegistry.getRealmService()
            ).also { instance = it }
        }
    }

    private val realm: Realm
        get() = realmService.getRealm()

    override fun getLastReadPositionFlow(mushafType: MushafType): Flow<LastReadPosition?> {
        return realm.query<LastReadPositionEntity>("mushafType == $0", mushafType.name)
            .asFlow()
            .map { results -> results.list.firstOrNull()?.toDomain() }
    }

    override suspend fun getLastReadPosition(mushafType: MushafType): LastReadPosition? = withContext(Dispatchers.IO) {
        realm.query<LastReadPositionEntity>("mushafType == $0", mushafType.name)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun updateLastReadPosition(
        mushafType: MushafType,
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        scrollPosition: Float
    ): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val existing = query<LastReadPositionEntity>("mushafType == $0", mushafType.name)
                .first()
                .find()

            if (existing != null) {
                existing.apply {
                    this.chapterNumber = chapterNumber
                    this.verseNumber = verseNumber
                    this.pageNumber = pageNumber
                    this.scrollPosition = scrollPosition
                    this.lastReadAt = System.currentTimeMillis()
                }
            } else {
                copyToRealm(LastReadPositionEntity().apply {
                    this.mushafType = mushafType.name
                    this.chapterNumber = chapterNumber
                    this.verseNumber = verseNumber
                    this.pageNumber = pageNumber
                    this.scrollPosition = scrollPosition
                    this.lastReadAt = System.currentTimeMillis()
                })
            }
        }
    }

    override suspend fun recordReadingSession(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        durationSeconds: Int,
        mushafType: MushafType
    ): Unit = withContext(Dispatchers.IO) {
        realm.write {
            copyToRealm(ReadingHistoryEntity().apply {
                this.chapterNumber = chapterNumber
                this.verseNumber = verseNumber
                this.pageNumber = pageNumber
                this.timestamp = System.currentTimeMillis()
                this.durationSeconds = durationSeconds
                this.mushafType = mushafType.name
            })
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<ReadingHistory> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getHistoryForDateRange(startTimestamp: Long, endTimestamp: Long): List<ReadingHistory> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>("timestamp >= $0 AND timestamp <= $1", startTimestamp, endTimestamp)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getHistoryForChapter(chapterNumber: Int): List<ReadingHistory> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>("chapterNumber == $0", chapterNumber)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun deleteHistoryOlderThan(timestamp: Long) = withContext(Dispatchers.IO) {
        realm.write {
            val oldHistory = query<ReadingHistoryEntity>("timestamp < $0", timestamp).find()
            delete(oldHistory)
        }
    }

    override suspend fun deleteAllHistory() = withContext(Dispatchers.IO) {
        realm.write {
            val history = query<ReadingHistoryEntity>().find()
            delete(history)
        }
    }

    override suspend fun getReadingStats(startTimestamp: Long?, endTimestamp: Long?): ReadingStats = withContext(Dispatchers.IO) {
        val history = if (startTimestamp != null && endTimestamp != null) {
            realm.query<ReadingHistoryEntity>("timestamp >= $0 AND timestamp <= $1", startTimestamp, endTimestamp).find()
        } else {
            realm.query<ReadingHistoryEntity>().find()
        }

        val totalTime = history.sumOf { it.durationSeconds.toLong() }
        val pages = history.map { it.pageNumber }.distinct().size
        val chapters = history.map { it.chapterNumber }.distinct().size
        val verses = history.map { "${it.chapterNumber}:${it.verseNumber}" }.distinct().size
        val mostRead = history.groupBy { it.chapterNumber }
            .maxByOrNull { it.value.size }?.key
        val currentStreak = calculateCurrentStreak(history.toList())
        val longestStreak = calculateLongestStreak(history.toList())
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
        realm.query<ReadingHistoryEntity>().find().sumOf { it.durationSeconds.toLong() }
    }

    override suspend fun getReadChapters(): List<Int> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>()
            .find()
            .map { it.chapterNumber }
            .distinct()
            .sorted()
    }

    override suspend fun getCurrentStreak(): Int = withContext(Dispatchers.IO) {
        val history = realm.query<ReadingHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
            .toList()
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

    private fun LastReadPositionEntity.toDomain() = LastReadPosition(
        mushafType = MushafType.valueOf(mushafType),
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
        mushafType = MushafType.valueOf(mushafType)
    )
}
