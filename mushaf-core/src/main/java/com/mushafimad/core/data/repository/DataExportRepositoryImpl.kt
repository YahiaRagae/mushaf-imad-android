package com.mushafimad.core.data.repository

import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of DataExportRepository
 * Internal implementation - not exposed in public API
 */
internal class DataExportRepositoryImpl private constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val reciterPreferencesRepository: ReciterPreferencesRepository,
    private val themeRepository: ThemeRepository
) : DataExportRepository {

    companion object {
        @Volatile private var instance: DataExportRepositoryImpl? = null

        fun getInstance(): DataExportRepository = instance ?: synchronized(this) {
            instance ?: DataExportRepositoryImpl(
                BookmarkRepositoryImpl.getInstance(),
                ReadingHistoryRepositoryImpl.getInstance(),
                SearchHistoryRepositoryImpl.getInstance(),
                PreferencesRepositoryImpl.getInstance(),
                ReciterPreferencesRepositoryImpl.getInstance(),
                ThemeRepositoryImpl.getInstance()
            ).also { instance = it }
        }
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportUserData(includeHistory: Boolean): UserDataBackup = withContext(Dispatchers.IO) {
        val bookmarks = bookmarkRepository.getAllBookmarks().map { it.toBackupData() }

        val lastReadPositions = MushafType.entries.mapNotNull { type ->
            readingHistoryRepository.getLastReadPosition(type)?.toBackupData()
        }

        val searchHistory = if (includeHistory) {
            searchHistoryRepository.getRecentSearches(limit = 500).map { it.toBackupData() }
        } else {
            emptyList()
        }

        val preferences = try {
            exportPreferences()
        } catch (e: Exception) {
            null
        }

        UserDataBackup(
            version = 1,
            timestamp = System.currentTimeMillis(),
            bookmarks = bookmarks,
            lastReadPositions = lastReadPositions,
            searchHistory = searchHistory,
            preferences = preferences
        )
    }

    override suspend fun exportToJson(includeHistory: Boolean): String = withContext(Dispatchers.IO) {
        val backup = exportUserData(includeHistory)
        json.encodeToString(backup)
    }

    override suspend fun importUserData(backup: UserDataBackup, mergeWithExisting: Boolean): ImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()

        // Clear existing data if not merging
        if (!mergeWithExisting) {
            try {
                bookmarkRepository.deleteAllBookmarks()
                searchHistoryRepository.clearSearchHistory()
            } catch (e: Exception) {
                errors.add("Failed to clear existing data: ${e.message}")
            }
        }

        // Import bookmarks
        var bookmarksImported = 0
        backup.bookmarks.forEach { bookmarkData ->
            try {
                bookmarkRepository.addBookmark(
                    chapterNumber = bookmarkData.chapterNumber,
                    verseNumber = bookmarkData.verseNumber,
                    pageNumber = bookmarkData.pageNumber,
                    note = bookmarkData.note,
                    tags = bookmarkData.tags
                )
                bookmarksImported++
            } catch (e: Exception) {
                errors.add("Failed to import bookmark ${bookmarkData.chapterNumber}:${bookmarkData.verseNumber}: ${e.message}")
            }
        }

        // Import last read positions
        var lastReadPositionsImported = 0
        backup.lastReadPositions.forEach { positionData ->
            try {
                val mushafType = MushafType.valueOf(positionData.mushafType)
                readingHistoryRepository.updateLastReadPosition(
                    mushafType = mushafType,
                    chapterNumber = positionData.chapterNumber,
                    verseNumber = positionData.verseNumber,
                    pageNumber = positionData.pageNumber,
                    scrollPosition = positionData.scrollPosition
                )
                lastReadPositionsImported++
            } catch (e: Exception) {
                errors.add("Failed to import last read position for ${positionData.mushafType}: ${e.message}")
            }
        }

        // Import search history
        var searchHistoryImported = 0
        backup.searchHistory.forEach { searchData ->
            try {
                val searchType = SearchType.valueOf(searchData.searchType)
                searchHistoryRepository.recordSearch(
                    query = searchData.query,
                    resultCount = searchData.resultCount,
                    searchType = searchType
                )
                searchHistoryImported++
            } catch (e: Exception) {
                errors.add("Failed to import search history: ${e.message}")
            }
        }

        // Import preferences
        var preferencesImported = false
        backup.preferences?.let { prefs ->
            try {
                importPreferences(prefs)
                preferencesImported = true
            } catch (e: Exception) {
                errors.add("Failed to import preferences: ${e.message}")
            }
        }

        ImportResult(
            bookmarksImported = bookmarksImported,
            lastReadPositionsImported = lastReadPositionsImported,
            searchHistoryImported = searchHistoryImported,
            preferencesImported = preferencesImported,
            errors = errors
        )
    }

    override suspend fun importFromJson(json: String, mergeWithExisting: Boolean): ImportResult = withContext(Dispatchers.IO) {
        try {
            val backup = this@DataExportRepositoryImpl.json.decodeFromString<UserDataBackup>(json)
            importUserData(backup, mergeWithExisting)
        } catch (e: Exception) {
            ImportResult(
                bookmarksImported = 0,
                lastReadPositionsImported = 0,
                searchHistoryImported = 0,
                preferencesImported = false,
                errors = listOf("Failed to parse JSON: ${e.message}")
            )
        }
    }

    override suspend fun clearAllUserData(): Unit = withContext(Dispatchers.IO) {
        bookmarkRepository.deleteAllBookmarks()
        readingHistoryRepository.deleteAllHistory()
        searchHistoryRepository.clearSearchHistory()
    }

    private suspend fun exportPreferences(): PreferencesData {
        val mushafType = preferencesRepository.getMushafTypeFlow().first()
        val currentPage = preferencesRepository.getCurrentPageFlow().first()
        val fontSize = preferencesRepository.getFontSizeMultiplierFlow().first()
        val reciterId = reciterPreferencesRepository.getSelectedReciterId()
        val playbackSpeed = reciterPreferencesRepository.getPlaybackSpeed()
        val repeatMode = reciterPreferencesRepository.getRepeatMode()
        val themeConfig = themeRepository.getThemeConfig()

        return PreferencesData(
            mushafType = mushafType.name,
            currentPage = currentPage,
            fontSizeMultiplier = fontSize,
            selectedReciterId = reciterId,
            playbackSpeed = playbackSpeed,
            repeatMode = repeatMode,
            themeMode = themeConfig.mode.name,
            colorScheme = themeConfig.colorScheme.name,
            useAmoled = themeConfig.useAmoled
        )
    }

    private suspend fun importPreferences(prefs: PreferencesData) {
        preferencesRepository.setMushafType(MushafType.valueOf(prefs.mushafType))
        preferencesRepository.setCurrentPage(prefs.currentPage)
        preferencesRepository.setFontSizeMultiplier(prefs.fontSizeMultiplier)
        reciterPreferencesRepository.setSelectedReciterId(prefs.selectedReciterId)
        reciterPreferencesRepository.setPlaybackSpeed(prefs.playbackSpeed)
        reciterPreferencesRepository.setRepeatMode(prefs.repeatMode)
        themeRepository.setThemeMode(ThemeMode.valueOf(prefs.themeMode))
        themeRepository.setColorScheme(ColorScheme.valueOf(prefs.colorScheme))
        themeRepository.setAmoledMode(prefs.useAmoled)
    }

    private fun Bookmark.toBackupData() = BookmarkData(
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        pageNumber = pageNumber,
        createdAt = createdAt,
        note = note,
        tags = tags
    )

    private fun LastReadPosition.toBackupData() = LastReadPositionData(
        mushafType = mushafType.name,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        pageNumber = pageNumber,
        lastReadAt = lastReadAt,
        scrollPosition = scrollPosition
    )

    private fun SearchHistoryEntry.toBackupData() = SearchHistoryData(
        query = query,
        timestamp = timestamp,
        resultCount = resultCount,
        searchType = searchType.name
    )
}
