package com.mushafimad.core.data.repository

import com.mushafimad.core.data.cache.ChaptersDataCache
import com.mushafimad.core.data.cache.CacheStats
import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.domain.models.Part
import com.mushafimad.core.domain.models.Quarter
import com.mushafimad.core.domain.repository.QuranRepository
import com.mushafimad.core.internal.ServiceRegistry

/**
 * Implementation of QuranRepository
 * Internal API - not exposed to library consumers
 */
internal class QuranRepositoryImpl private constructor(
    private val realmService: RealmService,
    private val chaptersDataCache: ChaptersDataCache,
    private val quranDataCacheService: QuranDataCacheService
) : QuranRepository {

    companion object {
        @Volatile private var instance: QuranRepositoryImpl? = null

        fun getInstance(): QuranRepository = instance ?: synchronized(this) {
            instance ?: QuranRepositoryImpl(
                ServiceRegistry.getRealmService(),
                ServiceRegistry.getChaptersCache(),
                ServiceRegistry.getQuranCacheService()
            ).also { instance = it }
        }
    }

    override suspend fun initialize() {
        realmService.initialize()
    }

    override fun isInitialized(): Boolean {
        return realmService.isInitialized
    }

    // Part Operations

    override suspend fun getAllParts(): List<Part> {
        return realmService.fetchAllPartsAsync()
    }

    override suspend fun getPart(number: Int): Part? {
        return realmService.getPart(number)
    }

    override suspend fun getPartForPage(pageNumber: Int): Part? {
        return realmService.getPartForPage(pageNumber)
    }

    override suspend fun getPartForVerse(chapterNumber: Int, verseNumber: Int): Part? {
        return realmService.getPartForVerse(chapterNumber, verseNumber)
    }

    // Quarter Operations

    override suspend fun getAllQuarters(): List<Quarter> {
        return realmService.fetchAllQuartersAsync()
    }

    override suspend fun getQuarter(hizbNumber: Int, fraction: Int): Quarter? {
        return realmService.getQuarter(hizbNumber, fraction)
    }

    override suspend fun getQuarterForPage(pageNumber: Int): Quarter? {
        return realmService.getQuarterForPage(pageNumber)
    }

    override suspend fun getQuarterForVerse(chapterNumber: Int, verseNumber: Int): Quarter? {
        return realmService.getQuarterForVerse(chapterNumber, verseNumber)
    }

    // Cache Management

    override suspend fun getCacheStats(): CacheStats {
        return quranDataCacheService.getCacheStats()
    }

    override suspend fun clearAllCaches() {
        chaptersDataCache.clearCache()
        quranDataCacheService.clearAllCache()
    }
}
