package com.mushafimad.core.data.repository

import com.mushafimad.core.data.local.entities.SearchHistoryEntity
import com.mushafimad.core.domain.models.SearchHistoryEntry
import com.mushafimad.core.domain.models.SearchSuggestion
import com.mushafimad.core.domain.models.SearchType
import com.mushafimad.core.domain.repository.SearchHistoryRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

/**
 * Implementation of SearchHistoryRepository using Realm
 * Internal implementation - not exposed in public API
 */
internal class SearchHistoryRepositoryImpl private constructor(
    private val realmService: RealmService
) : SearchHistoryRepository {

    companion object {
        @Volatile private var instance: SearchHistoryRepositoryImpl? = null

        fun getInstance(): SearchHistoryRepository = instance ?: synchronized(this) {
            instance ?: SearchHistoryRepositoryImpl(
                ServiceRegistry.getRealmService()
            ).also { instance = it }
        }
    }

    private val realm: Realm
        get() = realmService.getRealm()

    override fun getRecentSearchesFlow(limit: Int): Flow<List<SearchHistoryEntry>> {
        return realm.query<SearchHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    override suspend fun getRecentSearches(limit: Int): List<SearchHistoryEntry> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>()
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun recordSearch(
        query: String,
        resultCount: Int,
        searchType: SearchType
    ): Unit = withContext(Dispatchers.IO) {
        // Don't record empty queries
        if (query.isBlank()) return@withContext

        realm.write {
            copyToRealm(SearchHistoryEntity().apply {
                this.query = query.trim()
                this.timestamp = System.currentTimeMillis()
                this.resultCount = resultCount
                this.searchType = searchType.name
            })
        }

        // Clean up old history (keep last 100 entries)
        cleanupOldHistory(limit = 100)
    }

    override suspend fun getSearchSuggestions(
        prefix: String?,
        limit: Int
    ): List<SearchSuggestion> = withContext(Dispatchers.IO) {
        val searches = if (prefix != null && prefix.isNotBlank()) {
            realm.query<SearchHistoryEntity>()
                .find()
                .filter { it.query.startsWith(prefix, ignoreCase = true) }
        } else {
            realm.query<SearchHistoryEntity>().find()
        }

        // Group by query and count frequency
        searches
            .groupBy { it.query.lowercase() }
            .map { (query, entries) ->
                SearchSuggestion(
                    query = entries.first().query,
                    frequency = entries.size,
                    lastSearched = entries.maxOf { it.timestamp }
                )
            }
            .sortedWith(compareByDescending<SearchSuggestion> { it.frequency }
                .thenByDescending { it.lastSearched })
            .take(limit)
    }

    override suspend fun getPopularSearches(limit: Int): List<SearchSuggestion> = withContext(Dispatchers.IO) {
        val searches = realm.query<SearchHistoryEntity>().find()

        searches
            .groupBy { it.query.lowercase() }
            .map { (query, entries) ->
                SearchSuggestion(
                    query = entries.first().query,
                    frequency = entries.size,
                    lastSearched = entries.maxOf { it.timestamp }
                )
            }
            .sortedByDescending { it.frequency }
            .take(limit)
    }

    override suspend fun deleteSearch(id: String): Unit = withContext(Dispatchers.IO) {
        val objectId = try {
            ObjectId(id)
        } catch (e: Exception) {
            return@withContext
        }

        realm.write {
            val entity = query<SearchHistoryEntity>("id == $0", objectId)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteSearchesOlderThan(timestamp: Long): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val oldSearches = query<SearchHistoryEntity>("timestamp < $0", timestamp).find()
            delete(oldSearches)
        }
    }

    override suspend fun clearSearchHistory(): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val allSearches = query<SearchHistoryEntity>().find()
            delete(allSearches)
        }
    }

    override suspend fun getSearchesByType(searchType: SearchType, limit: Int): List<SearchHistoryEntry> = withContext(Dispatchers.IO) {
        realm.query<SearchHistoryEntity>("searchType == $0", searchType.name)
            .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
            .limit(limit)
            .find()
            .map { it.toDomain() }
    }

    /**
     * Clean up old history entries, keeping only the most recent ones
     */
    private suspend fun cleanupOldHistory(limit: Int) = withContext(Dispatchers.IO) {
        val count = realm.query<SearchHistoryEntity>().count().find()
        if (count > limit) {
            realm.write {
                val allEntries = query<SearchHistoryEntity>()
                    .sort("timestamp", io.realm.kotlin.query.Sort.DESCENDING)
                    .find()

                // Delete entries beyond the limit
                allEntries.drop(limit).forEach { entry ->
                    delete(entry)
                }
            }
        }
    }

    private fun SearchHistoryEntity.toDomain() = SearchHistoryEntry(
        id = id.toHexString(),
        query = query,
        timestamp = timestamp,
        resultCount = resultCount,
        searchType = SearchType.valueOf(searchType)
    )
}
