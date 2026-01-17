package com.mushafimad.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.UserDataBackup
import com.mushafimad.core.domain.repository.DataExportRepository
import com.mushafimad.core.domain.repository.ImportResult
import com.mushafimad.core.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for app settings and data management
 * Handles preferences, data export/import, and backup operations
 *
 * Dependencies are injected via Koin DI
 */
internal class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val dataExportRepository: DataExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Load all preferences
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                preferencesRepository.getMushafTypeFlow(),
                preferencesRepository.getCurrentPageFlow(),
                preferencesRepository.getFontSizeMultiplierFlow()
            ) { mushafType, currentPage, fontSize ->
                Triple(mushafType, currentPage, fontSize)
            }
                .catch { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to load preferences")
                    }
                }
                .collect { (mushafType, currentPage, fontSize) ->
                    _uiState.update {
                        it.copy(
                            mushafType = mushafType,
                            currentPage = currentPage,
                            fontSizeMultiplier = fontSize,
                            error = null
                        )
                    }
                }
        }
    }

    // === Preferences Management ===

    /**
     * Set mushaf type
     */
    fun setMushafType(type: MushafType) {
        viewModelScope.launch {
            try {
                preferencesRepository.setMushafType(type)
                _uiState.update {
                    it.copy(successMessage = "Mushaf type updated to ${type.name}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update mushaf type")
                }
            }
        }
    }

    /**
     * Set current page
     */
    fun setCurrentPage(page: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setCurrentPage(page)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update current page")
                }
            }
        }
    }

    /**
     * Set font size multiplier
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            try {
                preferencesRepository.setFontSizeMultiplier(multiplier)
                _uiState.update {
                    it.copy(successMessage = "Font size updated")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update font size")
                }
            }
        }
    }

    /**
     * Increase font size
     */
    fun increaseFontSize() {
        val currentSize = _uiState.value.fontSizeMultiplier
        val newSize = (currentSize + 0.1f).coerceAtMost(2.0f)
        setFontSizeMultiplier(newSize)
    }

    /**
     * Decrease font size
     */
    fun decreaseFontSize() {
        val currentSize = _uiState.value.fontSizeMultiplier
        val newSize = (currentSize - 0.1f).coerceAtLeast(0.5f)
        setFontSizeMultiplier(newSize)
    }

    /**
     * Reset font size to default
     */
    fun resetFontSize() {
        setFontSizeMultiplier(1.0f)
    }

    // === Data Export/Import ===

    /**
     * Export user data to JSON
     */
    fun exportData(includeHistory: Boolean = true) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, error = null) }

                val json = dataExportRepository.exportToJson(includeHistory)

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportedData = json,
                        successMessage = "Data exported successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = e.message ?: "Failed to export data"
                    )
                }
            }
        }
    }

    /**
     * Get backup object for advanced scenarios
     */
    fun getBackupData(includeHistory: Boolean = true) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, error = null) }

                val backup = dataExportRepository.exportUserData(includeHistory)

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        backupData = backup
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = e.message ?: "Failed to create backup"
                    )
                }
            }
        }
    }

    /**
     * Import user data from JSON
     */
    fun importData(json: String, mergeWithExisting: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isImporting = true, error = null) }

                val result = dataExportRepository.importFromJson(json, mergeWithExisting)

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = result,
                        successMessage = buildImportSuccessMessage(result)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = e.message ?: "Failed to import data"
                    )
                }
            }
        }
    }

    /**
     * Import from backup object
     */
    fun importFromBackup(backup: UserDataBackup, mergeWithExisting: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isImporting = true, error = null) }

                val result = dataExportRepository.importUserData(backup, mergeWithExisting)

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = result,
                        successMessage = buildImportSuccessMessage(result)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = e.message ?: "Failed to import data"
                    )
                }
            }
        }
    }

    /**
     * Clear all user data
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isClearing = true, error = null) }

                dataExportRepository.clearAllUserData()

                _uiState.update {
                    it.copy(
                        isClearing = false,
                        successMessage = "All user data cleared"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        error = e.message ?: "Failed to clear data"
                    )
                }
            }
        }
    }

    /**
     * Clear exported data from state
     */
    fun clearExportedData() {
        _uiState.update { it.copy(exportedData = null, backupData = null) }
    }

    /**
     * Clear import result from state
     */
    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Get available mushaf types
     */
    fun getAvailableMushafTypes(): List<MushafType> {
        return MushafType.entries
    }

    /**
     * Build success message from import result
     */
    private fun buildImportSuccessMessage(result: ImportResult): String {
        val parts = mutableListOf<String>()

        if (result.bookmarksImported > 0) {
            parts.add("${result.bookmarksImported} bookmarks")
        }
        if (result.lastReadPositionsImported > 0) {
            parts.add("${result.lastReadPositionsImported} reading positions")
        }
        if (result.searchHistoryImported > 0) {
            parts.add("${result.searchHistoryImported} search entries")
        }
        if (result.preferencesImported) {
            parts.add("preferences")
        }

        val imported = if (parts.isNotEmpty()) {
            "Imported: ${parts.joinToString(", ")}"
        } else {
            "No data imported"
        }

        val errors = if (result.errors.isNotEmpty()) {
            "\nErrors: ${result.errors.size}"
        } else {
            ""
        }

        return imported + errors
    }
}

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    // Preferences
    val mushafType: MushafType = MushafType.HAFS_1441,
    val currentPage: Int = 1,
    val fontSizeMultiplier: Float = 1.0f,

    // Export/Import state
    val exportedData: String? = null,
    val backupData: UserDataBackup? = null,
    val importResult: ImportResult? = null,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isClearing: Boolean = false,

    // Messages
    val error: String? = null,
    val successMessage: String? = null
)
