package com.mushafimad.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Unified search ViewModel
 * Combines searching across verses, chapters, reciters, and bookmarks
 * Manages search history and suggestions
 *
 * Dependencies are injected via Koin DI
 */
class SearchViewModel(
    private val verseRepository: VerseRepository,
    private val chapterRepository: ChapterRepository,
    private val audioRepository: AudioRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadSearchHistory()
        loadSearchSuggestions()
    }

    /**
     * Perform a unified search across all types
     */
    fun search(query: String, searchType: SearchType = SearchType.GENERAL) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        _uiState.update { it.copy(query = query, isSearching = true, error = null) }

        viewModelScope.launch {
            try {
                val results = when (searchType) {
                    SearchType.VERSE -> searchVerses(query)
                    SearchType.CHAPTER -> searchChapters(query)
                    SearchType.GENERAL -> searchAll(query)
                }

                // Record search in history
                val totalResults = results.verseResults.size +
                        results.chapterResults.size +
                        results.reciterResults.size +
                        results.bookmarkResults.size

                searchHistoryRepository.recordSearch(
                    query = query,
                    resultCount = totalResults,
                    searchType = searchType
                )

                _uiState.update {
                    it.copy(
                        results = results,
                        isSearching = false,
                        hasSearched = true
                    )
                }

                // Refresh suggestions after recording search
                loadSearchSuggestions()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "Search failed"
                    )
                }
            }
        }
    }

    /**
     * Search only verses
     */
    private suspend fun searchVerses(query: String): SearchResults {
        val verses = verseRepository.searchVerses(query)
        return SearchResults(verseResults = verses)
    }

    /**
     * Search only chapters
     */
    private suspend fun searchChapters(query: String): SearchResults {
        val chapters = chapterRepository.searchChapters(query)
        return SearchResults(chapterResults = chapters)
    }

    /**
     * Search only reciters
     */
    private suspend fun searchReciters(query: String): SearchResults {
        val reciters = audioRepository.searchReciters(query)
        return SearchResults(reciterResults = reciters)
    }

    /**
     * Search only bookmarks
     */
    private suspend fun searchBookmarks(query: String): SearchResults {
        val bookmarks = bookmarkRepository.searchBookmarks(query)
        return SearchResults(bookmarkResults = bookmarks)
    }

    /**
     * Search across all types
     */
    private suspend fun searchAll(query: String): SearchResults {
        return try {
            SearchResults(
                verseResults = verseRepository.searchVerses(query),
                chapterResults = chapterRepository.searchChapters(query),
                reciterResults = audioRepository.searchReciters(query),
                bookmarkResults = bookmarkRepository.searchBookmarks(query)
            )
        } catch (e: Exception) {
            SearchResults()
        }
    }

    /**
     * Load search history
     */
    private fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                val history = searchHistoryRepository.getRecentSearches(limit = 20)
                _uiState.update { it.copy(searchHistory = history) }
            } catch (e: Exception) {
                // Silent failure for history
            }
        }
    }

    /**
     * Load search suggestions based on prefix
     */
    fun loadSearchSuggestions(prefix: String? = null) {
        viewModelScope.launch {
            try {
                val suggestions = searchHistoryRepository.getSearchSuggestions(
                    prefix = prefix,
                    limit = 10
                )
                _uiState.update { it.copy(suggestions = suggestions) }
            } catch (e: Exception) {
                // Silent failure for suggestions
            }
        }
    }

    /**
     * Get popular searches
     */
    fun loadPopularSearches() {
        viewModelScope.launch {
            try {
                val popular = searchHistoryRepository.getPopularSearches(limit = 10)
                _uiState.update { it.copy(popularSearches = popular) }
            } catch (e: Exception) {
                // Silent failure
            }
        }
    }

    /**
     * Delete a search history entry
     */
    fun deleteSearchHistoryEntry(id: String) {
        viewModelScope.launch {
            try {
                searchHistoryRepository.deleteSearch(id)
                loadSearchHistory()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete search history")
                }
            }
        }
    }

    /**
     * Clear all search history
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                searchHistoryRepository.clearSearchHistory()
                _uiState.update { it.copy(searchHistory = emptyList(), suggestions = emptyList()) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to clear search history")
                }
            }
        }
    }

    /**
     * Clear current search
     */
    fun clearSearch() {
        _uiState.update {
            it.copy(
                query = "",
                results = SearchResults(),
                hasSearched = false,
                error = null
            )
        }
    }

    /**
     * Set active search filter
     */
    fun setSearchFilter(searchType: SearchType?) {
        _uiState.update { it.copy(activeFilter = searchType) }
        // Re-run search with new filter if there's an active query
        if (_uiState.value.hasSearched && _uiState.value.query.isNotBlank()) {
            search(_uiState.value.query, searchType ?: SearchType.GENERAL)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for search screen
 */
data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val searchHistory: List<SearchHistoryEntry> = emptyList(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val popularSearches: List<SearchSuggestion> = emptyList(),
    val activeFilter: SearchType? = null,
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
)

/**
 * Combined search results across all types
 */
data class SearchResults(
    val verseResults: List<Verse> = emptyList(),
    val chapterResults: List<Chapter> = emptyList(),
    val reciterResults: List<ReciterInfo> = emptyList(),
    val bookmarkResults: List<Bookmark> = emptyList()
) {
    val isEmpty: Boolean
        get() = verseResults.isEmpty() && chapterResults.isEmpty() &&
                reciterResults.isEmpty() && bookmarkResults.isEmpty()

    val totalCount: Int
        get() = verseResults.size + chapterResults.size +
                reciterResults.size + bookmarkResults.size
}
