package com.mushafimad.core.data.local.dao

import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import com.mushafimad.core.data.local.entities.ReadingHistoryEntity
import com.mushafimad.core.data.repository.RealmService
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Realm implementation of ReadingHistoryDao
 * All Realm-specific operations are encapsulated here
 *
 * @internal This class is not part of the public API
 */
internal class RealmReadingHistoryDao(
    private val realmService: RealmService
) : ReadingHistoryDao {

    private val realm: Realm
        get() = realmService.getUserRealm()

    // Last Read Position operations

    override fun getLastReadPositionFlow(mushafType: String): Flow<LastReadPositionEntity?> {
        return realm.query<LastReadPositionEntity>("mushafType == $0", mushafType)
            .asFlow()
            .map { results -> results.list.firstOrNull() }
    }

    override suspend fun getLastReadPosition(mushafType: String): LastReadPositionEntity? = withContext(Dispatchers.IO) {
        realm.query<LastReadPositionEntity>("mushafType == $0", mushafType)
            .first()
            .find()
    }

    override suspend fun updateLastReadPosition(
        mushafType: String,
        pageNumber: Int,
        chapterNumber: Int,
        verseNumber: Int,
        timestamp: Long
    ): Unit = withContext(Dispatchers.IO) {
        realm.write {
            // Find or create last read position
            val existing = query<LastReadPositionEntity>("mushafType == $0", mushafType)
                .first()
                .find()

            if (existing != null) {
                // Update existing
                existing.apply {
                    this.pageNumber = pageNumber
                    this.chapterNumber = chapterNumber
                    this.verseNumber = verseNumber
                    this.lastReadAt = timestamp
                }
            } else {
                // Create new
                val entity = LastReadPositionEntity().apply {
                    this.mushafType = mushafType
                    this.pageNumber = pageNumber
                    this.chapterNumber = chapterNumber
                    this.verseNumber = verseNumber
                    this.lastReadAt = timestamp
                }
                copyToRealm(entity)
            }
        }
    }

    // Reading History operations

    override suspend fun insertHistory(
        chapterNumber: Int,
        pageNumber: Int,
        timestamp: Long,
        durationSeconds: Int
    ): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entity = ReadingHistoryEntity().apply {
                this.chapterNumber = chapterNumber
                this.pageNumber = pageNumber
                this.timestamp = timestamp
                this.durationSeconds = durationSeconds
            }
            copyToRealm(entity)
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<ReadingHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
    }

    override suspend fun getHistoryForDateRange(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<ReadingHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>("timestamp >= $0 AND timestamp <= $1", startTimestamp, endTimestamp)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
    }

    override suspend fun getHistoryForChapter(chapterNumber: Int): List<ReadingHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>("chapterNumber == $0", chapterNumber)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
    }

    override suspend fun getAllHistory(): List<ReadingHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<ReadingHistoryEntity>().find()
    }

    override suspend fun deleteHistoryOlderThan(timestamp: Long) = withContext(Dispatchers.IO) {
        realm.write {
            val entriesToDelete = query<ReadingHistoryEntity>("timestamp < $0", timestamp).find()
            delete(entriesToDelete)
        }
    }

    override suspend fun deleteAllHistory() = withContext(Dispatchers.IO) {
        realm.write {
            val allHistory = query<ReadingHistoryEntity>().find()
            delete(allHistory)
        }
    }
}
