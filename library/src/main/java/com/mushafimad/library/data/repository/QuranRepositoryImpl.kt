package com.mushafimad.library.data.repository

import com.mushafimad.library.data.cache.ChaptersDataCache
import com.mushafimad.library.data.cache.CacheStats
import com.mushafimad.library.data.cache.QuranDataCacheService
import com.mushafimad.library.domain.models.Part
import com.mushafimad.library.domain.models.Quarter
import com.mushafimad.library.domain.repository.QuranRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of QuranRepository
 * Internal API - not exposed to library consumers
 */
@Singleton
internal class QuranRepositoryImpl @Inject constructor(
    private val realmService: RealmService,
    private val chaptersDataCache: ChaptersDataCache,
    private val quranDataCacheService: QuranDataCacheService
) : QuranRepository {

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
