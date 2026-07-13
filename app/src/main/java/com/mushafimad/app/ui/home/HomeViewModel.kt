package com.mushafimad.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.models.LastReadPosition
import com.mushafimad.core.domain.models.MushafType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    val lastRead: LastReadPosition? = null,
    val lastReadChapterName: String? = null,
    val error: String? = null,
)

class HomeViewModel : ViewModel() {

    // Repositories come straight from the library singleton - no DI setup in the app.
    private val chapterRepository = MushafLibrary.getChapterRepository()
    private val readingHistoryRepository = MushafLibrary.getReadingHistoryRepository()
    private val preferencesRepository = MushafLibrary.getPreferencesRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching {
                val chapters = chapterRepository.getAllChapters()
                val mushafType = MushafType.HAFS_1441
                val lastRead = readingHistoryRepository.getLastReadPosition(mushafType)
                val name = lastRead?.let { chapterRepository.getChapter(it.chapterNumber)?.englishTitle }
                HomeUiState(
                    loading = false,
                    chapters = chapters,
                    lastRead = lastRead,
                    lastReadChapterName = name
                )
            }.onSuccess { _uiState.value = it }
                .onFailure { _uiState.value = HomeUiState(loading = false, error = it.message ?: it.toString()) }
        }
    }
}
