package com.mushafimad.core.di

import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.*
import org.koin.dsl.module

/**
 * Koin module for core library dependencies
 * Provides repositories as singletons using ServiceRegistry under the hood
 *
 * This module bridges Koin DI with the internal ServiceRegistry pattern:
 * - Koin manages ViewModel dependencies and scoping
 * - ServiceRegistry manages internal service singletons
 * - Repositories delegate to MushafLibrary.get*Repository()
 */
val coreModule = module {
    // Quran Data Repositories
    single<QuranRepository> { MushafLibrary.getQuranRepository() }
    single<ChapterRepository> { MushafLibrary.getChapterRepository() }
    single<PageRepository> { MushafLibrary.getPageRepository() }
    single<VerseRepository> { MushafLibrary.getVerseRepository() }

    // User Data Repositories
    single<BookmarkRepository> { MushafLibrary.getBookmarkRepository() }
    single<ReadingHistoryRepository> { MushafLibrary.getReadingHistoryRepository() }
    single<SearchHistoryRepository> { MushafLibrary.getSearchHistoryRepository() }

    // Audio Repositories
    single<AudioRepository> { MushafLibrary.getAudioRepository() }

    // Preferences Repositories
    single<PreferencesRepository> { MushafLibrary.getPreferencesRepository() }
    single<ReciterPreferencesRepository> { MushafLibrary.getReciterPreferencesRepository() }
    single<ThemeRepository> { MushafLibrary.getThemeRepository() }

    // Data Export Repository
    single<DataExportRepository> { MushafLibrary.getDataExportRepository() }
}
