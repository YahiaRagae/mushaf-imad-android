package com.mushafimad.core.data.local.dao

import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import com.mushafimad.core.data.local.entities.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reading History entities
 * Abstracts Realm operations for testing and flexibility
 *
 * @internal This interface is not part of the public API
 */
internal interface ReadingHistoryDao {

    // Last Read Position operations
    fun getLastReadPositionFlow(mushafType: String): Flow<LastReadPositionEntity?>
    suspend fun getLastReadPosition(mushafType: String): LastReadPositionEntity?
    suspend fun updateLastReadPosition(
        mushafType: String,
        pageNumber: Int,
        chapterNumber: Int,
        verseNumber: Int,
        timestamp: Long
    )

    // Reading History operations
    suspend fun insertHistory(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        timestamp: Long,
        durationSeconds: Int,
        mushafType: String
    )

    suspend fun getRecentHistory(limit: Int): List<ReadingHistoryEntity>

    suspend fun getHistoryForDateRange(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<ReadingHistoryEntity>

    suspend fun getHistoryForChapter(chapterNumber: Int): List<ReadingHistoryEntity>

    suspend fun getAllHistory(): List<ReadingHistoryEntity>

    suspend fun deleteHistoryOlderThan(timestamp: Long)

    suspend fun deleteAllHistory()
}
