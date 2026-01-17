package com.mushafimad.ui.mushaf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Mushaf (Quran) page display and navigation
 * Manages page state, verse selection, navigation, and reading position
 */
class MushafViewModel(
    private val verseRepository: VerseRepository = MushafLibrary.getVerseRepository(),
    private val chapterRepository: ChapterRepository = MushafLibrary.getChapterRepository(),
    private val readingHistoryRepository: ReadingHistoryRepository = MushafLibrary.getReadingHistoryRepository(),
    private val preferencesRepository: PreferencesRepository = MushafLibrary.getPreferencesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Load user preferences and restore last read position
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                // Get mushaf type from flow
                val mushafType = preferencesRepository.getMushafTypeFlow().first()
                val currentPage = preferencesRepository.getCurrentPageFlow().first()

                // Get last read position
                val lastPosition = readingHistoryRepository.getLastReadPosition(mushafType)

                _uiState.update {
                    it.copy(
                        mushafType = mushafType,
                        currentPage = lastPosition?.pageNumber ?: currentPage,
                        currentChapter = lastPosition?.chapterNumber ?: 1,
                        currentVerse = lastPosition?.verseNumber ?: 1,
                        scrollPosition = lastPosition?.scrollPosition ?: 0f
                    )
                }

                // Load initial page
                loadPage(_uiState.value.currentPage)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load preferences")
                }
            }
        }
    }

    /**
     * Load a specific page
     */
    fun loadPage(pageNumber: Int) {
        if (pageNumber < 1 || pageNumber > 604) {
            _uiState.update { it.copy(error = "Invalid page number: $pageNumber") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val mushafType = _uiState.value.mushafType
                println("MushafViewModel: Loading page $pageNumber with mushafType $mushafType")
                val verses = verseRepository.getVersesForPage(pageNumber, mushafType)
                println("MushafViewModel: Got ${verses.size} verses for page $pageNumber")

                if (verses.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No verses found for page $pageNumber"
                        )
                    }
                    return@launch
                }

                // Get chapter info for page header
                val chapterNumbers = verses.map { it.chapterNumber }.distinct()
                val chapters = chapterNumbers.mapNotNull { chapterNum ->
                    try {
                        chapterRepository.getChapter(chapterNum)
                    } catch (e: Exception) {
                        null
                    }
                }

                _uiState.update {
                    it.copy(
                        currentPage = pageNumber,
                        verses = verses,
                        chapters = chapters,
                        isLoading = false,
                        error = null
                    )
                }

                // Update preferences
                preferencesRepository.setCurrentPage(pageNumber)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load page"
                    )
                }
            }
        }
    }

    /**
     * Navigate to next page
     */
    fun nextPage() {
        val nextPage = _uiState.value.currentPage + 1
        if (nextPage <= 604) {
            loadPage(nextPage)
        }
    }

    /**
     * Navigate to previous page
     */
    fun previousPage() {
        val prevPage = _uiState.value.currentPage - 1
        if (prevPage >= 1) {
            loadPage(prevPage)
        }
    }

    /**
     * Go to specific chapter
     */
    fun goToChapter(chapterNumber: Int, verseNumber: Int = 1) {
        if (chapterNumber < 1 || chapterNumber > 114) {
            _uiState.update { it.copy(error = "Invalid chapter number: $chapterNumber") }
            return
        }

        viewModelScope.launch {
            try {
                val verse = verseRepository.getVerse(chapterNumber, verseNumber)

                if (verse != null) {
                    loadPage(verse.pageNumber)
                    _uiState.update {
                        it.copy(
                            currentChapter = chapterNumber,
                            currentVerse = verseNumber
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "Verse not found: $chapterNumber:$verseNumber")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to navigate to chapter")
                }
            }
        }
    }

    /**
     * Go to specific verse
     */
    fun goToVerse(chapterNumber: Int, verseNumber: Int) {
        goToChapter(chapterNumber, verseNumber)
    }

    /**
     * Go to specific page
     */
    fun goToPage(pageNumber: Int) {
        loadPage(pageNumber)
    }

    /**
     * Select a verse for highlighting or actions
     */
    fun selectVerse(verse: Verse) {
        _uiState.update {
            it.copy(
                selectedVerse = verse,
                currentChapter = verse.chapterNumber,
                currentVerse = verse.number
            )
        }
    }

    /**
     * Clear verse selection
     */
    fun clearSelection() {
        _uiState.update { it.copy(selectedVerse = null) }
    }

    /**
     * Toggle verse in selection (for multi-select)
     */
    fun toggleVerseSelection(verse: Verse) {
        val currentSelections = _uiState.value.selectedVerses.toMutableSet()

        if (currentSelections.contains(verse)) {
            currentSelections.remove(verse)
        } else {
            currentSelections.add(verse)
        }

        _uiState.update { it.copy(selectedVerses = currentSelections) }
    }

    /**
     * Clear all verse selections
     */
    fun clearAllSelections() {
        _uiState.update { it.copy(selectedVerses = emptySet()) }
    }

    /**
     * Set mushaf type
     */
    fun setMushafType(type: MushafType) {
        viewModelScope.launch {
            try {
                preferencesRepository.setMushafType(type)
                _uiState.update { it.copy(mushafType = type) }

                // Reload current page with new mushaf type
                loadPage(_uiState.value.currentPage)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to change mushaf type")
                }
            }
        }
    }

    /**
     * Update scroll position for current page
     */
    fun updateScrollPosition(position: Float) {
        _uiState.update { it.copy(scrollPosition = position) }
    }

    /**
     * Save current reading position
     */
    fun saveReadingPosition() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                readingHistoryRepository.updateLastReadPosition(
                    mushafType = state.mushafType,
                    chapterNumber = state.currentChapter,
                    verseNumber = state.currentVerse,
                    pageNumber = state.currentPage,
                    scrollPosition = state.scrollPosition
                )
            } catch (e: Exception) {
                // Silent failure for saving position
            }
        }
    }

    /**
     * Record reading session
     */
    fun recordReadingSession(durationSeconds: Int) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                readingHistoryRepository.recordReadingSession(
                    chapterNumber = state.currentChapter,
                    verseNumber = state.currentVerse,
                    pageNumber = state.currentPage,
                    durationSeconds = durationSeconds,
                    mushafType = state.mushafType
                )
            } catch (e: Exception) {
                // Silent failure for recording session
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Get page info for display
     */
    fun getPageInfo(): PageInfo {
        val state = _uiState.value
        return PageInfo(
            pageNumber = state.currentPage,
            totalPages = 604,
            chapterName = state.chapters.firstOrNull()?.arabicTitle ?: "",
            juzNumber = calculateJuzNumber(state.currentPage),
            progress = (state.currentPage.toFloat() / 604 * 100).toInt()
        )
    }

    /**
     * Calculate Juz number from page number
     * Each Juz is approximately 20 pages
     */
    private fun calculateJuzNumber(pageNumber: Int): Int {
        return ((pageNumber - 1) / 20) + 1
    }
}

/**
 * UI state for Mushaf view
 */
data class MushafUiState(
    val mushafType: MushafType = MushafType.HAFS_1441,
    val currentPage: Int = 1,
    val currentChapter: Int = 1,
    val currentVerse: Int = 1,
    val scrollPosition: Float = 0f,

    val verses: List<Verse> = emptyList(),
    val chapters: List<Chapter> = emptyList(),

    val selectedVerse: Verse? = null,
    val selectedVerses: Set<Verse> = emptySet(),

    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Page information for display
 */
data class PageInfo(
    val pageNumber: Int,
    val totalPages: Int,
    val chapterName: String,
    val juzNumber: Int,
    val progress: Int
)
