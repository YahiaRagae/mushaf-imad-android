package com.mushafimad.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.domain.models.LastReadPosition
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReadingStats
import com.mushafimad.core.domain.repository.ReadingHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for reading history and statistics
 * Provides UI state for reading progress, streaks, and statistics
 *
 * Dependencies are injected via Koin DI
 */
internal class ReadingHistoryViewModel(
    private val readingHistoryRepository: ReadingHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingHistoryUiState())
    val uiState: StateFlow<ReadingHistoryUiState> = _uiState.asStateFlow()

    init {
        loadReadingStatistics()
        loadLastReadPositions()
    }

    /**
     * Load reading statistics
     */
    fun loadReadingStatistics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val stats = readingHistoryRepository.getReadingStats()

                _uiState.update {
                    it.copy(
                        statistics = stats,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load statistics"
                    )
                }
            }
        }
    }

    /**
     * Load last read positions for all mushaf types
     */
    fun loadLastReadPositions() {
        viewModelScope.launch {
            try {
                val positions = mutableMapOf<MushafType, LastReadPosition>()

                MushafType.entries.forEach { type ->
                    val position = readingHistoryRepository.getLastReadPosition(type)
                    if (position != null) {
                        positions[type] = position
                    }
                }

                _uiState.update { it.copy(lastReadPositions = positions) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load last read positions")
                }
            }
        }
    }

    /**
     * Get last read position for specific mushaf type
     */
    fun getLastReadPosition(mushafType: MushafType) {
        viewModelScope.launch {
            try {
                val position = readingHistoryRepository.getLastReadPosition(mushafType)
                _uiState.update { state ->
                    val updatedPositions = state.lastReadPositions.toMutableMap()
                    if (position != null) {
                        updatedPositions[mushafType] = position
                    }
                    state.copy(
                        lastReadPositions = updatedPositions,
                        selectedPosition = position
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load last read position")
                }
            }
        }
    }

    /**
     * Update last read position
     */
    fun updateLastReadPosition(
        mushafType: MushafType,
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        scrollPosition: Float = 0f
    ) {
        viewModelScope.launch {
            try {
                readingHistoryRepository.updateLastReadPosition(
                    mushafType = mushafType,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    pageNumber = pageNumber,
                    scrollPosition = scrollPosition
                )

                // Refresh last read positions
                loadLastReadPositions()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update last read position")
                }
            }
        }
    }

    /**
     * Record reading session
     */
    fun recordReadingSession(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        durationSeconds: Int = 0,
        mushafType: MushafType
    ) {
        viewModelScope.launch {
            try {
                readingHistoryRepository.recordReadingSession(
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    pageNumber = pageNumber,
                    durationSeconds = durationSeconds,
                    mushafType = mushafType
                )

                // Refresh statistics and positions
                loadReadingStatistics()
                loadLastReadPositions()

                _uiState.update {
                    it.copy(successMessage = "Reading session recorded")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to record reading session")
                }
            }
        }
    }

    /**
     * Delete all reading history
     */
    fun deleteAllHistory() {
        viewModelScope.launch {
            try {
                readingHistoryRepository.deleteAllHistory()

                _uiState.update {
                    it.copy(
                        statistics = ReadingStats(
                            totalReadingTimeSeconds = 0,
                            totalPagesRead = 0,
                            totalChaptersRead = 0,
                            totalVersesRead = 0,
                            mostReadChapter = null,
                            currentStreak = 0,
                            longestStreak = 0,
                            averageDailyMinutes = 0
                        ),
                        lastReadPositions = emptyMap(),
                        successMessage = "Reading history cleared"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete reading history")
                }
            }
        }
    }

    /**
     * Get streak information
     */
    fun getStreakInfo(): StreakInfo {
        val stats = _uiState.value.statistics
        return StreakInfo(
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak,
            isOnStreak = stats.currentStreak > 0,
            daysToNextMilestone = calculateDaysToNextMilestone(stats.currentStreak)
        )
    }

    /**
     * Get reading progress summary
     */
    fun getProgressSummary(): ProgressSummary {
        val stats = _uiState.value.statistics
        val totalVerses = 6236 // Total verses in Quran

        return ProgressSummary(
            totalSessions = 0, // Not tracked in ReadingStats
            uniqueChaptersRead = stats.totalChaptersRead,
            totalTimeMinutes = stats.totalReadingTimeSeconds / 60,
            progressPercentage = if (totalVerses > 0) {
                (stats.totalVersesRead.toFloat() / totalVerses * 100).toInt()
            } else 0,
            averageSessionMinutes = stats.averageDailyMinutes
        )
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Clear selected position
     */
    fun clearSelectedPosition() {
        _uiState.update { it.copy(selectedPosition = null) }
    }

    /**
     * Calculate days to next streak milestone
     */
    private fun calculateDaysToNextMilestone(currentStreak: Int): Int {
        val milestones = listOf(7, 30, 60, 90, 180, 365)
        return milestones.firstOrNull { it > currentStreak }?.let { it - currentStreak } ?: 0
    }

    /**
     * Format streak display
     */
    fun formatStreakDisplay(days: Int): String {
        return when {
            days == 0 -> "Start your streak!"
            days == 1 -> "1 day streak"
            days < 7 -> "$days days streak"
            days < 30 -> "${days / 7} ${if (days / 7 == 1) "week" else "weeks"} streak"
            days < 365 -> "${days / 30} ${if (days / 30 == 1) "month" else "months"} streak"
            else -> "${days / 365} ${if (days / 365 == 1) "year" else "years"} streak"
        }
    }

    /**
     * Format reading time
     */
    fun formatReadingTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }
}

/**
 * UI state for reading history screen
 */
data class ReadingHistoryUiState(
    val statistics: ReadingStats = ReadingStats(
        totalReadingTimeSeconds = 0,
        totalPagesRead = 0,
        totalChaptersRead = 0,
        totalVersesRead = 0,
        mostReadChapter = null,
        currentStreak = 0,
        longestStreak = 0,
        averageDailyMinutes = 0
    ),
    val lastReadPositions: Map<MushafType, LastReadPosition> = emptyMap(),
    val selectedPosition: LastReadPosition? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Streak information
 */
data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val isOnStreak: Boolean,
    val daysToNextMilestone: Int
)

/**
 * Reading progress summary
 */
data class ProgressSummary(
    val totalSessions: Int,
    val uniqueChaptersRead: Int,
    val totalTimeMinutes: Long,
    val progressPercentage: Int,
    val averageSessionMinutes: Int
)
