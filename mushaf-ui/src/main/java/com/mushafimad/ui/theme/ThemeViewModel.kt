package com.mushafimad.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ColorScheme
import com.mushafimad.core.domain.models.ThemeConfig
import com.mushafimad.core.domain.models.ThemeMode
import com.mushafimad.core.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app theme and appearance
 * Provides UI state and operations for theme customization
 */
internal class ThemeViewModel(
    private val themeRepository: ThemeRepository = MushafLibrary.getThemeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        loadThemeConfig()
    }

    /**
     * Load current theme configuration
     */
    private fun loadThemeConfig() {
        viewModelScope.launch {
            themeRepository.getThemeConfigFlow()
                .catch { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to load theme config")
                    }
                }
                .collect { config ->
                    _uiState.update {
                        it.copy(
                            themeConfig = config,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Set theme mode (light, dark, or system)
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                themeRepository.setThemeMode(mode)
                _uiState.update {
                    it.copy(successMessage = "Theme mode updated")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update theme mode")
                }
            }
        }
    }

    /**
     * Set color scheme
     */
    fun setColorScheme(scheme: ColorScheme) {
        viewModelScope.launch {
            try {
                themeRepository.setColorScheme(scheme)
                _uiState.update {
                    it.copy(successMessage = "Color scheme updated")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update color scheme")
                }
            }
        }
    }

    /**
     * Toggle AMOLED mode (true black backgrounds for dark theme)
     */
    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                themeRepository.setAmoledMode(enabled)
                _uiState.update {
                    it.copy(
                        successMessage = if (enabled) "AMOLED mode enabled" else "AMOLED mode disabled"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update AMOLED mode")
                }
            }
        }
    }

    /**
     * Cycle to next theme mode
     */
    fun cycleThemeMode() {
        val currentMode = _uiState.value.themeConfig.mode
        val nextMode = when (currentMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    /**
     * Cycle to next color scheme
     */
    fun cycleColorScheme() {
        val currentScheme = _uiState.value.themeConfig.colorScheme
        val nextScheme = when (currentScheme) {
            ColorScheme.DEFAULT -> ColorScheme.WARM
            ColorScheme.WARM -> ColorScheme.COOL
            ColorScheme.COOL -> ColorScheme.SEPIA
            ColorScheme.SEPIA -> ColorScheme.DEFAULT
        }
        setColorScheme(nextScheme)
    }

    /**
     * Reset theme to defaults
     */
    fun resetTheme() {
        viewModelScope.launch {
            try {
                themeRepository.setThemeMode(ThemeMode.SYSTEM)
                themeRepository.setColorScheme(ColorScheme.DEFAULT)
                themeRepository.setAmoledMode(false)
                _uiState.update {
                    it.copy(successMessage = "Theme reset to defaults")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to reset theme")
                }
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Get theme mode display name
     */
    fun getThemeModeDisplayName(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System Default"
        }
    }

    /**
     * Get color scheme display name
     */
    fun getColorSchemeDisplayName(scheme: ColorScheme): String {
        return when (scheme) {
            ColorScheme.DEFAULT -> "Default"
            ColorScheme.WARM -> "Warm"
            ColorScheme.COOL -> "Cool"
            ColorScheme.SEPIA -> "Sepia"
        }
    }

    /**
     * Get available theme modes
     */
    fun getAvailableThemeModes(): List<ThemeMode> {
        return ThemeMode.entries
    }

    /**
     * Get available color schemes
     */
    fun getAvailableColorSchemes(): List<ColorScheme> {
        return ColorScheme.entries
    }
}

/**
 * UI state for theme screen
 */
data class ThemeUiState(
    val themeConfig: ThemeConfig = ThemeConfig(),
    val error: String? = null,
    val successMessage: String? = null
)
