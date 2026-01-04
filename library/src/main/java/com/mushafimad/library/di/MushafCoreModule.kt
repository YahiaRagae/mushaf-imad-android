package com.mushafimad.library.di

import android.content.Context
import com.mushafimad.library.data.cache.ChaptersDataCache
import com.mushafimad.library.data.cache.QuranDataCacheService
import com.mushafimad.library.data.repository.*
import com.mushafimad.library.domain.repository.ChapterRepository
import com.mushafimad.library.domain.repository.PageRepository
import com.mushafimad.library.domain.repository.QuranRepository
import com.mushafimad.library.domain.repository.VerseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing core dependencies for the Mushaf library
 * Includes database access, caching, and core services
 */
@Module
@InstallIn(SingletonComponent::class)
object MushafCoreModule {

    @Provides
    @Singleton
    internal fun provideRealmService(
        @ApplicationContext context: Context
    ): RealmService {
        return RealmServiceImpl(context)
    }

    @Provides
    @Singleton
    internal fun provideChaptersDataCache(
        realmService: RealmService
    ): ChaptersDataCache {
        return ChaptersDataCache(realmService)
    }

    @Provides
    @Singleton
    internal fun provideQuranDataCacheService(
        realmService: RealmService
    ): QuranDataCacheService {
        return QuranDataCacheService(realmService)
    }

    // Repository Providers

    @Provides
    @Singleton
    internal fun provideChapterRepository(
        realmService: RealmService,
        chaptersDataCache: ChaptersDataCache
    ): ChapterRepository {
        return ChapterRepositoryImpl(realmService, chaptersDataCache)
    }

    @Provides
    @Singleton
    internal fun providePageRepository(
        realmService: RealmService,
        cacheService: QuranDataCacheService
    ): PageRepository {
        return PageRepositoryImpl(realmService, cacheService)
    }

    @Provides
    @Singleton
    internal fun provideVerseRepository(
        realmService: RealmService,
        cacheService: QuranDataCacheService
    ): VerseRepository {
        return VerseRepositoryImpl(realmService, cacheService)
    }

    @Provides
    @Singleton
    internal fun provideQuranRepository(
        realmService: RealmService,
        chaptersDataCache: ChaptersDataCache,
        quranDataCacheService: QuranDataCacheService
    ): QuranRepository {
        return QuranRepositoryImpl(realmService, chaptersDataCache, quranDataCacheService)
    }
}
