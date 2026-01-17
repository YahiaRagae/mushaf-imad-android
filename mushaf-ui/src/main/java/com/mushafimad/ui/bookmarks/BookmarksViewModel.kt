package com.mushafimad.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Bookmark
import com.mushafimad.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing bookmarks
 * Provides UI state and operations for bookmark management
 */
internal class BookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository = MushafLibrary.getBookmarkRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    /**
     * Load all bookmarks
     */
    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            bookmarkRepository.getAllBookmarksFlow()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load bookmarks"
                        )
                    }
                }
                .collect { bookmarks ->
                    _uiState.update {
                        it.copy(
                            bookmarks = bookmarks,
                            filteredBookmarks = filterBookmarks(bookmarks, it.searchQuery, it.selectedTag),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Add a new bookmark
     */
    fun addBookmark(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        note: String = "",
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                bookmarkRepository.addBookmark(
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    pageNumber = pageNumber,
                    note = note,
                    tags = tags
                )
                _uiState.update { it.copy(successMessage = "Bookmark added successfully") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to add bookmark")
                }
            }
        }
    }

    /**
     * Update bookmark note
     */
    fun updateBookmarkNote(id: String, note: String) {
        viewModelScope.launch {
            try {
                bookmarkRepository.updateBookmarkNote(id, note)
                _uiState.update { it.copy(successMessage = "Note updated successfully") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update note")
                }
            }
        }
    }

    /**
     * Update bookmark tags
     */
    fun updateBookmarkTags(id: String, tags: List<String>) {
        viewModelScope.launch {
            try {
                bookmarkRepository.updateBookmarkTags(id, tags)
                _uiState.update { it.copy(successMessage = "Tags updated successfully") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update tags")
                }
            }
        }
    }

    /**
     * Delete a bookmark
     */
    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            try {
                bookmarkRepository.deleteBookmark(id)
                _uiState.update { it.copy(successMessage = "Bookmark deleted successfully") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete bookmark")
                }
            }
        }
    }

    /**
     * Search bookmarks by query
     */
    fun searchBookmarks(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            _uiState.update { state ->
                state.copy(filteredBookmarks = filterBookmarks(state.bookmarks, "", state.selectedTag))
            }
            return
        }

        viewModelScope.launch {
            try {
                val results = bookmarkRepository.searchBookmarks(query)
                _uiState.update { state ->
                    state.copy(
                        filteredBookmarks = filterBookmarks(results, query, state.selectedTag)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Search failed")
                }
            }
        }
    }

    /**
     * Filter bookmarks by tag
     */
    fun filterByTag(tag: String?) {
        _uiState.update { state ->
            state.copy(
                selectedTag = tag,
                filteredBookmarks = filterBookmarks(state.bookmarks, state.searchQuery, tag)
            )
        }
    }

    /**
     * Get all available tags from bookmarks
     */
    fun loadTags() {
        viewModelScope.launch {
            try {
                val allBookmarks = bookmarkRepository.getAllBookmarks()
                val tags = allBookmarks
                    .flatMap { it.tags }
                    .distinct()
                    .sorted()
                _uiState.update { it.copy(availableTags = tags) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load tags")
                }
            }
        }
    }

    /**
     * Get bookmark for a specific verse
     */
    fun getBookmarkForVerse(chapterNumber: Int, verseNumber: Int) {
        viewModelScope.launch {
            try {
                val bookmark = bookmarkRepository.getBookmarkForVerse(chapterNumber, verseNumber)
                _uiState.update { it.copy(selectedBookmark = bookmark) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load bookmark")
                }
            }
        }
    }

    /**
     * Clear any error or success messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Helper function to filter bookmarks
     */
    private fun filterBookmarks(
        bookmarks: List<Bookmark>,
        query: String,
        tag: String?
    ): List<Bookmark> {
        var filtered = bookmarks

        // Filter by tag
        if (tag != null) {
            filtered = filtered.filter { it.tags.contains(tag) }
        }

        // Filter by search query (if not already searched via repository)
        if (query.isNotBlank()) {
            filtered = filtered.filter { bookmark ->
                bookmark.note.contains(query, ignoreCase = true) ||
                bookmark.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        return filtered
    }
}

/**
 * UI state for bookmarks screen
 */
data class BookmarksUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val filteredBookmarks: List<Bookmark> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedBookmark: Bookmark? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
