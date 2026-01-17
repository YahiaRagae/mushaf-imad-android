package com.mushafimad.sampleapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel demonstrating ChapterRepository usage
 */
class ChaptersViewModel(
    private val chapterRepository: ChapterRepository = MushafLibrary.getChapterRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChaptersUiState>(ChaptersUiState.Loading)
    val uiState: StateFlow<ChaptersUiState> = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    fun loadChapters() {
        viewModelScope.launch {
            _uiState.value = ChaptersUiState.Loading

            try {
                // Load and cache chapters with progress
                chapterRepository.loadAndCacheChapters { count ->
                    // Progress callback (optional)
                }

                // Get all chapters
                val chapters = chapterRepository.getAllChapters()
                _uiState.value = ChaptersUiState.Success(chapters)
            } catch (e: Exception) {
                _uiState.value = ChaptersUiState.Error(e.message ?: "Failed to load chapters")
            }
        }
    }

    fun searchChapters(query: String) {
        if (query.isBlank()) {
            loadChapters()
            return
        }

        viewModelScope.launch {
            _uiState.value = ChaptersUiState.Loading

            try {
                val results = chapterRepository.searchChapters(query)
                _uiState.value = ChaptersUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = ChaptersUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun getChapter(number: Int) {
        viewModelScope.launch {
            try {
                val chapter = chapterRepository.getChapter(number)
                if (chapter != null) {
                    _uiState.value = ChaptersUiState.Success(listOf(chapter))
                } else {
                    _uiState.value = ChaptersUiState.Error("Chapter not found")
                }
            } catch (e: Exception) {
                _uiState.value = ChaptersUiState.Error(e.message ?: "Failed to load chapter")
            }
        }
    }
}

sealed class ChaptersUiState {
    data object Loading : ChaptersUiState()
    data class Success(val chapters: List<Chapter>) : ChaptersUiState()
    data class Error(val message: String) : ChaptersUiState()
}
