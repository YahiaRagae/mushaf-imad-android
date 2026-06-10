package com.mushafimad.core.di

import com.mushafimad.core.data.audio.AyahTimingService
import com.mushafimad.core.data.audio.MediaSessionManager
import com.mushafimad.core.data.audio.ReciterService
import com.mushafimad.core.data.cache.ChaptersDataCache
import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.data.local.dao.BookmarkDao
import com.mushafimad.core.data.local.dao.ReadingHistoryDao
import com.mushafimad.core.data.local.dao.RealmBookmarkDao
import com.mushafimad.core.data.local.dao.RealmReadingHistoryDao
import com.mushafimad.core.data.local.dao.RealmSearchHistoryDao
import com.mushafimad.core.data.local.dao.SearchHistoryDao
import com.mushafimad.core.data.repository.*
import com.mushafimad.core.domain.repository.*
import org.koin.dsl.module

/**
 * Koin module for core library dependencies
 * Provides all services and repositories as singletons with proper dependency injection
 *
 * Architecture:
 * - Koin manages ALL dependencies (no ServiceRegistry)
 * - Infrastructure services (Realm, cache, audio) created directly
 * - Repositories receive dependencies via constructor injection
 * - Testable: all services can be swapped with fakes/mocks
 */
val coreModule = module {
    // Infrastructure services - created directly by Koin

    // Context (provided by ContentProvider during initialization)
    // No definition needed here - will be provided via androidContext()

    // Realm service (useInMemory = false for production, can be overridden in tests)
    single<RealmService> {
        DefaultRealmService(
            context = get(),
            useInMemory = false
        )
    }

    // Cache services
    single { ChaptersDataCache(get()) }
    single { QuranDataCacheService(get()) }

    // Audio services
    single { AyahTimingService(get()) }
    single { ReciterService(get(), get(), get()) }
    single {
        MediaSessionManager(get()).also {
            it.initialize()
        }
    }

    // Data Access Objects (DAOs) - abstraction layer for database operations
    single<BookmarkDao> { RealmBookmarkDao(get()) }
    single<ReadingHistoryDao> { RealmReadingHistoryDao(get()) }
    single<SearchHistoryDao> { RealmSearchHistoryDao(get()) }

    // Quran Data Repositories
    single<QuranRepository> {
        DefaultQuranRepository(
            realmService = get(),
            chaptersDataCache = get(),
            quranDataCacheService = get()
        )
    }
    single<ChapterRepository> {
        DefaultChapterRepository(
            realmService = get(),
            chaptersDataCache = get()
        )
    }
    single<PageRepository> {
        DefaultPageRepository(
            realmService = get(),
            cacheService = get()
        )
    }
    single<VerseRepository> {
        DefaultVerseRepository(
            realmService = get(),
            cacheService = get()
        )
    }

    // User Data Repositories (fully using DAO abstraction - no direct Realm access)
    single<BookmarkRepository> {
        DefaultBookmarkRepository(get())  // Injects BookmarkDao
    }
    single<ReadingHistoryRepository> {
        DefaultReadingHistoryRepository(get())  // Injects ReadingHistoryDao
    }
    single<SearchHistoryRepository> {
        DefaultSearchHistoryRepository(get())  // Injects SearchHistoryDao
    }

    // Audio Repository
    single<AudioRepository> {
        DefaultAudioRepository(get(), get(), get())
    }

    // Preferences Repository (consolidated - includes Mushaf, Audio, and Theme preferences)
    single<PreferencesRepository> {
        DefaultPreferencesRepository(get())
    }

    // Data Export Repository
    single<DataExportRepository> {
        DefaultDataExportRepository(get(), get(), get(), get())
    }
}
