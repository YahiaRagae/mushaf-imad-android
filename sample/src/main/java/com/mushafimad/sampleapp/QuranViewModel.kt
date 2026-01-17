package com.mushafimad.sampleapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.PageHeaderInfo
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.core.domain.repository.PageRepository
import com.mushafimad.core.domain.repository.PreferencesRepository
import com.mushafimad.core.domain.repository.QuranRepository
import com.mushafimad.core.domain.repository.VerseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel demonstrating page, verse, and preferences usage
 */
class QuranViewModel(
    private val pageRepository: PageRepository = MushafLibrary.getPageRepository(),
    private val verseRepository: VerseRepository = MushafLibrary.getVerseRepository(),
    private val quranRepository: QuranRepository = MushafLibrary.getQuranRepository(),
    private val preferencesRepository: PreferencesRepository = MushafLibrary.getPreferencesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuranUiState>(QuranUiState.Loading)
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    // Preferences flows
    val mushafType = preferencesRepository.getMushafTypeFlow()
    val currentPage = preferencesRepository.getCurrentPageFlow()
    val fontSizeMultiplier = preferencesRepository.getFontSizeMultiplierFlow()

    init {
        observeCurrentPage()
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            try {
                quranRepository.initialize()
            } catch (e: Exception) {
                _uiState.value = QuranUiState.Error("Failed to initialize database: ${e.message}")
            }
        }
    }

    private fun observeCurrentPage() {
        viewModelScope.launch {
            combine(
                currentPage,
                mushafType
            ) { page, type ->
                Pair(page, type)
            }.collect { (page, type) ->
                loadPage(page, type)
            }
        }
    }

    private suspend fun loadPage(pageNumber: Int, mushafType: MushafType) {
        _uiState.value = QuranUiState.Loading

        try {
            // Load page header
            val header = pageRepository.getPageHeaderInfo(pageNumber, mushafType)

            // Load verses for this page
            val verses = verseRepository.getVersesForPage(pageNumber, mushafType)

            // Cache adjacent pages for smooth scrolling
            if (pageNumber > 1) {
                pageRepository.cachePage(pageNumber - 1)
            }
            if (pageNumber < 604) {
                pageRepository.cachePage(pageNumber + 1)
            }

            _uiState.value = QuranUiState.Success(
                pageNumber = pageNumber,
                verses = verses,
                header = header
            )
        } catch (e: Exception) {
            _uiState.value = QuranUiState.Error(e.message ?: "Failed to load page")
        }
    }

    fun goToPage(pageNumber: Int) {
        viewModelScope.launch {
            preferencesRepository.setCurrentPage(pageNumber)
        }
    }

    fun nextPage() {
        viewModelScope.launch {
            val current = currentPage.first()
            if (current < 604) {
                preferencesRepository.setCurrentPage(current + 1)
            }
        }
    }

    fun previousPage() {
        viewModelScope.launch {
            val current = currentPage.first()
            if (current > 1) {
                preferencesRepository.setCurrentPage(current - 1)
            }
        }
    }

    fun setMushafType(type: MushafType) {
        viewModelScope.launch {
            preferencesRepository.setMushafType(type)
        }
    }

    fun setFontSize(multiplier: Float) {
        viewModelScope.launch {
            preferencesRepository.setFontSizeMultiplier(multiplier)
        }
    }

    fun searchVerses(query: String) {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading

            try {
                val results = verseRepository.searchVerses(query)
                _uiState.value = QuranUiState.SearchResults(results)
            } catch (e: Exception) {
                _uiState.value = QuranUiState.Error(e.message ?: "Search failed")
            }
        }
    }
}

sealed class QuranUiState {
    data object Loading : QuranUiState()
    data class Success(
        val pageNumber: Int,
        val verses: List<Verse>,
        val header: PageHeaderInfo?
    ) : QuranUiState()
    data class SearchResults(val verses: List<Verse>) : QuranUiState()
    data class Error(val message: String) : QuranUiState()
}
