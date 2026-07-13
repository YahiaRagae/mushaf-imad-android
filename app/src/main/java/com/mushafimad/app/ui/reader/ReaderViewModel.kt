package com.mushafimad.app.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Verse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StartPage {
    data object Resolving : StartPage

    /** [page] == null means: let the library restore the last read position. */
    data class Ready(val page: Int?) : StartPage
}

data class ReaderUiState(
    val startPage: StartPage = StartPage.Resolving,
    val title: String = "Reader",
    val currentPage: Int? = null,
    val message: String? = null,
)

class ReaderViewModel : ViewModel() {

    private val chapterRepository = MushafLibrary.getChapterRepository()
    private val verseRepository = MushafLibrary.getVerseRepository()
    private val bookmarkRepository = MushafLibrary.getBookmarkRepository()
    private val preferencesRepository = MushafLibrary.getPreferencesRepository()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    val mushafType: StateFlow<MushafType> = preferencesRepository.getMushafTypeFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MushafType.HAFS_1441)

    private var resolved = false

    /**
     * chapterNumber > 0 -> open at that surah's first page
     * requestedPage > 0 -> open at that exact page
     * neither           -> resume the last read position (initialPage = null)
     */
    fun resolveStart(chapterNumber: Int, requestedPage: Int) {
        if (resolved) return
        resolved = true
        viewModelScope.launch {
            when {
                requestedPage > 0 -> {
                    val chapter = chapterRepository.getChapterForPage(requestedPage)
                    _uiState.value = _uiState.value.copy(
                        startPage = StartPage.Ready(requestedPage),
                        title = chapter?.englishTitle ?: "Page $requestedPage",
                        currentPage = requestedPage
                    )
                }

                chapterNumber > 0 -> {
                    val chapter = chapterRepository.getChapter(chapterNumber)
                    // FRICTION: Chapter has no startPage, so the consumer has to look up
                    // the first verse of the surah to find which page it begins on.
                    val page = verseRepository.getVerse(chapterNumber, 1)?.pageNumber
                        ?: verseRepository.getVersesForChapter(chapterNumber).firstOrNull()?.pageNumber
                        ?: 1
                    _uiState.value = _uiState.value.copy(
                        startPage = StartPage.Ready(page),
                        title = chapter?.englishTitle ?: "Surah $chapterNumber",
                        currentPage = page
                    )
                }

                else -> _uiState.value = _uiState.value.copy(
                    startPage = StartPage.Ready(null),
                    title = "Continue reading"
                )
            }
        }
    }

    fun onPageChanged(page: Int) {
        viewModelScope.launch {
            val chapter = chapterRepository.getChapterForPage(page)
            _uiState.value = _uiState.value.copy(
                currentPage = page,
                title = chapter?.englishTitle ?: "Page $page"
            )
        }
    }

    /** Bookmarks come from the library's BookmarkRepository. */
    fun toggleBookmark(verse: Verse) {
        viewModelScope.launch {
            val existing = bookmarkRepository.getBookmarkForVerse(verse.chapterNumber, verse.number)
            if (existing != null) {
                bookmarkRepository.deleteBookmark(existing.id)
                showMessage("Removed bookmark ${verse.chapterNumber}:${verse.number}")
            } else {
                bookmarkRepository.addBookmark(
                    chapterNumber = verse.chapterNumber,
                    verseNumber = verse.number,
                    pageNumber = verse.pageNumber,
                    note = "Saved from reader"
                )
                showMessage("Bookmarked ${verse.chapterNumber}:${verse.number}")
            }
        }
    }

    private fun showMessage(text: String) {
        _uiState.value = _uiState.value.copy(message = text)
    }

    fun messageShown() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
