package com.mushafimad.core.data.local.dao

import com.mushafimad.core.data.local.entities.SearchHistoryEntity
import com.mushafimad.core.data.repository.RealmService
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

/**
 * Realm implementation of SearchHistoryDao
 * All Realm-specific operations are encapsulated here
 *
 * @internal This class is not part of the public API
 */
internal class RealmSearchHistoryDao(
    private val realmService: RealmService
) : SearchHistoryDao {

    private val realm: Realm
        get() = realmService.getUserRealm()

    override fun getRecentSearchesFlow(limit: Int): Flow<List<SearchHistoryEntity>> {
        return realm.query<SearchHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .asFlow()
            .map { results -> results.list }
    }

    override suspend fun getRecentSearches(limit: Int): List<SearchHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
    }

    override suspend fun getAllSearches(): List<SearchHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>().find()
    }

    override suspend fun insertSearch(
        query: String,
        resultCount: Int,
        searchType: String,
        timestamp: Long
    ): ObjectId = withContext(Dispatchers.IO) {
        realm.write {
            val entity = SearchHistoryEntity().apply {
                this.query = query
                this.resultCount = resultCount
                this.searchType = searchType
                this.timestamp = timestamp
            }
            copyToRealm(entity).id
        }
    }

    override suspend fun getByPrefix(prefix: String): List<SearchHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>()
            .find()
            .filter { it.query.startsWith(prefix, ignoreCase = true) }
    }

    override suspend fun getByType(searchType: String, limit: Int): List<SearchHistoryEntity> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>("searchType == $0", searchType)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
    }

    override suspend fun delete(id: ObjectId): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<SearchHistoryEntity>("id == $0", id)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteOlderThan(timestamp: Long): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entriesToDelete = query<SearchHistoryEntity>("timestamp < $0", timestamp).find()
            delete(entriesToDelete)
        }
    }

    override suspend fun deleteAll(): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val allSearches = query<SearchHistoryEntity>().find()
            delete(allSearches)
        }
    }

    override suspend fun getCount(): Long = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>().count().find()
    }

    override suspend fun removeOldestEntries(keepCount: Int): Unit = withContext(Dispatchers.IO) {
        val count = getCount()
        if (count > keepCount) {
            realm.write {
                val allEntries = query<SearchHistoryEntity>()
                    .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
                    .find()

                // Delete entries beyond the keepCount
                allEntries.drop(keepCount).forEach { entry ->
                    delete(entry)
                }
            }
        }
    }
}
