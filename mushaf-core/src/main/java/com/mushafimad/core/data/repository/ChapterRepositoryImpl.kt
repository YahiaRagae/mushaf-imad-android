package com.mushafimad.core.data.repository

import com.mushafimad.core.data.cache.ChaptersDataCache
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.models.ChaptersByHizb
import com.mushafimad.core.domain.models.ChaptersByPart
import com.mushafimad.core.domain.models.ChaptersByType
import com.mushafimad.core.domain.repository.ChapterRepository
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of ChapterRepository
 * Internal API - not exposed to library consumers
 */
internal class ChapterRepositoryImpl private constructor(
    private val realmService: RealmService,
    private val chaptersDataCache: ChaptersDataCache
) : ChapterRepository {

    companion object {
        @Volatile private var instance: ChapterRepositoryImpl? = null

        fun getInstance(): ChapterRepository = instance ?: synchronized(this) {
            instance ?: ChapterRepositoryImpl(
                ServiceRegistry.getRealmService(),
                ServiceRegistry.getChaptersCache()
            ).also { instance = it }
        }
    }

    override fun getAllChaptersFlow(): Flow<List<Chapter>> = flow {
        // Emit cached data if available
        if (chaptersDataCache.isCached) {
            emit(chaptersDataCache.allChapters)
        }

        // Load and cache if not already cached
        if (!chaptersDataCache.isCached) {
            chaptersDataCache.loadAndCache()
            emit(chaptersDataCache.allChapters)
        }
    }

    override suspend fun getAllChapters(): List<Chapter> {
        // Return from cache if available
        if (chaptersDataCache.isCached && chaptersDataCache.allChapters.isNotEmpty()) {
            return chaptersDataCache.allChapters
        }

        // Otherwise load from database
        return realmService.fetchAllChaptersAsync()
    }

    override suspend fun getChapter(number: Int): Chapter? {
        return realmService.getChapter(number)
    }

    override suspend fun getChapterForPage(pageNumber: Int): Chapter? {
        return realmService.getChapterForPage(pageNumber)
    }

    override suspend fun getChaptersOnPage(pageNumber: Int): List<Chapter> {
        return realmService.getChaptersOnPage(pageNumber)
    }

    override suspend fun searchChapters(query: String): List<Chapter> {
        return realmService.searchChapters(query)
    }

    override suspend fun getChaptersByPart(): List<ChaptersByPart> {
        // Ensure chapters are cached first
        if (!chaptersDataCache.isCached) {
            chaptersDataCache.loadAndCache()
        }

        // Load parts grouping if not cached
        if (!chaptersDataCache.isPartsCached) {
            chaptersDataCache.loadPartsGrouping()
        }

        return chaptersDataCache.allChaptersByPart
    }

    override suspend fun getChaptersByHizb(): List<ChaptersByHizb> {
        // Ensure chapters are cached first
        if (!chaptersDataCache.isCached) {
            chaptersDataCache.loadAndCache()
        }

        // Load hizb grouping if not cached
        if (!chaptersDataCache.isHizbCached) {
            chaptersDataCache.loadQuartersGrouping()
        }

        return chaptersDataCache.allChaptersByHizb
    }

    override suspend fun getChaptersByType(): List<ChaptersByType> {
        // Ensure chapters are cached first
        if (!chaptersDataCache.isCached) {
            chaptersDataCache.loadAndCache()
        }

        // Load type grouping if not cached
        if (!chaptersDataCache.isTypeCached) {
            chaptersDataCache.loadTypesGrouping()
        }

        return chaptersDataCache.allChaptersByType
    }

    override suspend fun loadAndCacheChapters(onProgress: ((Int) -> Unit)?) {
        chaptersDataCache.loadAndCache(onProgress)
    }

    override suspend fun clearCache() {
        chaptersDataCache.clearCache()
    }
}
