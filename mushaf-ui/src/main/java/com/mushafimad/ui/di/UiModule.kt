package com.mushafimad.ui.di

import com.mushafimad.ui.bookmarks.BookmarksViewModel
import com.mushafimad.ui.history.ReadingHistoryViewModel
import com.mushafimad.ui.mushaf.MushafViewModel
import com.mushafimad.ui.player.QuranPlayerViewModel
import com.mushafimad.ui.search.SearchViewModel
import com.mushafimad.ui.settings.SettingsViewModel
import com.mushafimad.ui.theme.ThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for UI layer ViewModels
 * ViewModels are properly scoped and receive repositories via Koin
 *
 * This module ensures:
 * - Single ViewModel instances per scope (no duplicates)
 * - Proper lifecycle management
 * - Easy testing (can inject mock repositories)
 * - Dependencies injected via constructor
 */
val uiModule = module {
    // Mushaf ViewModel (4 dependencies)
    viewModel {
        MushafViewModel(
            verseRepository = get(),
            chapterRepository = get(),
            readingHistoryRepository = get(),
            preferencesRepository = get()
        )
    }

    // Player ViewModel (1 dependency)
    viewModel {
        QuranPlayerViewModel(
            audioRepository = get()
        )
    }

    // Search ViewModel (5 dependencies)
    viewModel {
        SearchViewModel(
            verseRepository = get(),
            chapterRepository = get(),
            audioRepository = get(),
            bookmarkRepository = get(),
            searchHistoryRepository = get()
        )
    }

    // Bookmarks ViewModel (1 dependency)
    viewModel {
        BookmarksViewModel(
            bookmarkRepository = get()
        )
    }

    // Reading History ViewModel (1 dependency)
    viewModel {
        ReadingHistoryViewModel(
            readingHistoryRepository = get()
        )
    }

    // Settings ViewModel (2 dependencies)
    viewModel {
        SettingsViewModel(
            preferencesRepository = get(),
            dataExportRepository = get()
        )
    }

    // Theme ViewModel (1 dependency)
    viewModel {
        ThemeViewModel(
            themeRepository = get()
        )
    }
}
