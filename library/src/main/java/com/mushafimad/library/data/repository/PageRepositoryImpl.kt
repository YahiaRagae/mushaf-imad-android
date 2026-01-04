package com.mushafimad.library.data.repository

import com.mushafimad.library.data.cache.QuranDataCacheService
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Page
import com.mushafimad.library.domain.models.PageHeaderInfo
import com.mushafimad.library.domain.repository.PageRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PageRepository
 * Internal API - not exposed to library consumers
 */
@Singleton
internal class PageRepositoryImpl @Inject constructor(
    private val realmService: RealmService,
    private val cacheService: QuranDataCacheService
) : PageRepository {

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
