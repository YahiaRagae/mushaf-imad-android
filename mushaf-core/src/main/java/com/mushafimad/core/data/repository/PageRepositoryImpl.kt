package com.mushafimad.core.data.repository

import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Page
import com.mushafimad.core.domain.models.PageHeaderInfo
import com.mushafimad.core.domain.repository.PageRepository
import com.mushafimad.core.internal.ServiceRegistry

/**
 * Implementation of PageRepository
 * Internal API - not exposed to library consumers
 */
internal class PageRepositoryImpl private constructor(
    private val realmService: RealmService,
    private val cacheService: QuranDataCacheService
) : PageRepository {

    companion object {
        @Volatile private var instance: PageRepositoryImpl? = null

        fun getInstance(): PageRepository = instance ?: synchronized(this) {
            instance ?: PageRepositoryImpl(
                ServiceRegistry.getRealmService(),
                ServiceRegistry.getQuranCacheService()
            ).also { instance = it }
        }
    }

    override suspend fun getPage(number: Int): Page? {
        return realmService.getPage(number)
    }

    override suspend fun getTotalPages(): Int {
        return realmService.getTotalPages()
    }

    override suspend fun getPageHeaderInfo(pageNumber: Int, mushafType: MushafType): PageHeaderInfo? {
        // Try cache first
        val cached = cacheService.getCachedPageHeader(pageNumber)
        if (cached != null) {
            return cached
        }

        // Otherwise fetch from database
        return realmService.getPageHeaderInfo(pageNumber, mushafType)
    }

    override suspend fun cachePage(pageNumber: Int) {
        cacheService.cachePageData(pageNumber)
    }

    override suspend fun cachePageRange(pageRange: IntRange) {
        cacheService.cachePageRange(pageRange)
    }

    override suspend fun isPageCached(pageNumber: Int): Boolean {
        return cacheService.isPageCached(pageNumber)
    }

    override suspend fun clearPageCache(pageNumber: Int) {
        cacheService.clearPageCache(pageNumber)
    }

    override suspend fun clearAllPageCache() {
        cacheService.clearAllCache()
    }
}
