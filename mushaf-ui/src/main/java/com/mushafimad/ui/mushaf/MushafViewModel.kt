package com.mushafimad.ui.mushaf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Mushaf (Quran) page display and navigation
 * Manages page state, verse selection, navigation, and reading position
 *
 * Dependencies are injected via Koin DI
 */
class MushafViewModel(
    private val verseRepository: VerseRepository,
    private val chapterRepository: ChapterRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    // Per-page content for the pager; bounded so swiping through the whole
    // mushaf can't grow memory without limit
    private val pageCache = android.util.LruCache<Int, PageContent>(PAGE_CACHE_SIZE)

    // True once the consumer (initialPage) or the user navigated somewhere;
    // the async last-position restore must not override that
    private var navigationRequested = false

    private var savePositionJob: Job? = null

    init {
        loadPreferences()
    }

    /**
     * Cache lookup without loading; lets the pager render a neighbouring page
     * instantly when it was already prefetched.
     */
    internal fun peekPageContent(pageNumber: Int): PageContent? = pageCache.get(pageNumber)

    /**
     * Load (or fetch from cache) the content of a single page without
     * touching the shared UI state. Used by the pager for off-screen pages.
     */
    internal suspend fun pageContent(pageNumber: Int): PageContent? {
        if (pageNumber < 1 || pageNumber > TOTAL_PAGES) return null
        pageCache.get(pageNumber)?.let { return it }

        return try {
            val mushafType = _uiState.value.mushafType
            val verses = verseRepository.getVersesForPage(pageNumber, mushafType)
            if (verses.isEmpty()) return null

            val chapters = verses.map { it.chapterNumber }.distinct().mapNotNull { chapterNum ->
                try {
                    chapterRepository.getChapter(chapterNum)
                } catch (e: Exception) {
                    null
                }
            }

            PageContent(verses, chapters).also { pageCache.put(pageNumber, it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Commit a page the user swiped to once the pager settles on it.
     * Unlike [loadPage], never flips isLoading: the pager has already
     * rendered the page, so a full-screen spinner here would flash.
     */
    internal fun onPageSettled(pageNumber: Int) {
        val state = _uiState.value
        if (pageNumber == state.currentPage && state.verses.isNotEmpty()) return

        navigationRequested = true

        viewModelScope.launch {
            val content = pageContent(pageNumber) ?: return@launch
            _uiState.update {
                it.copy(
                    currentPage = pageNumber,
                    currentChapter = content.verses.first().chapterNumber,
                    currentVerse = content.verses.first().number,
                    verses = content.verses,
                    chapters = content.chapters,
                    isLoading = false,
                    error = null
                )
            }
            try {
                preferencesRepository.setCurrentPage(pageNumber)
            } catch (e: Exception) {
                // Silent failure for preference persistence
            }
            schedulePositionSave()
        }
    }

    /**
     * Persist the reading position shortly after it stabilises. Debounced so
     * fast consecutive swipes write once; replaces the old 30-second timer,
     * which lost the position on process death.
     */
    private fun schedulePositionSave() {
        savePositionJob?.cancel()
        savePositionJob = viewModelScope.launch {
            delay(SAVE_POSITION_DEBOUNCE_MS)
            saveReadingPosition()
        }
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

                // A consumer-provided initialPage (or early user navigation)
                // takes precedence over the restored position
                if (navigationRequested) return@launch

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
        if (pageNumber < 1 || pageNumber > TOTAL_PAGES) {
            _uiState.update { it.copy(error = "Invalid page number: $pageNumber") }
            return
        }

        navigationRequested = true

        // Only show the full-screen loader when there is nothing on screen
        // yet; programmatic navigation animates the pager instead.
        if (_uiState.value.verses.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }

        viewModelScope.launch {
            try {
                val content = pageContent(pageNumber)

                if (content == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No verses found for page $pageNumber"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        currentPage = pageNumber,
                        currentChapter = content.verses.first().chapterNumber,
                        currentVerse = content.verses.first().number,
                        verses = content.verses,
                        chapters = content.chapters,
                        isLoading = false,
                        error = null
                    )
                }

                // Update preferences
                preferencesRepository.setCurrentPage(pageNumber)
                schedulePositionSave()
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
        if (nextPage <= TOTAL_PAGES) {
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
        if (chapterNumber < 1 || chapterNumber > TOTAL_CHAPTERS) {
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

                // Cached pages were rendered for the previous layout
                pageCache.evictAll()

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
        schedulePositionSave()
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
            totalPages = TOTAL_PAGES,
            chapterName = state.chapters.firstOrNull()?.arabicTitle ?: "",
            juzNumber = calculateJuzNumber(state.currentPage),
            progress = (state.currentPage.toFloat() / TOTAL_PAGES * 100).toInt()
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

/**
 * Verses and chapter metadata of a single mushaf page, as consumed by the
 * pager in MushafView.
 *
 * @internal Not part of the public API.
 */
internal data class PageContent(
    val verses: List<Verse>,
    val chapters: List<Chapter>
)

internal val TOTAL_PAGES = com.mushafimad.core.utils.QuranUtils.TOTAL_PAGES
internal val TOTAL_CHAPTERS = com.mushafimad.core.utils.QuranUtils.TOTAL_CHAPTERS
private const val PAGE_CACHE_SIZE = 8
private const val SAVE_POSITION_DEBOUNCE_MS = 1000L
