package com.mushafimad.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ColorScheme
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * App-wide reading appearance.
 *
 * NOTE (library friction): the library persists a [ColorScheme] via
 * PreferencesRepository, but its composables take a *different* enum,
 * [ColorSchemeType], and the reading background ([ReadingTheme]) is not
 * persisted by the library at all. So the consumer has to map the first pair
 * and store the second itself.
 */
object AppSettings {

    private lateinit var prefs: SharedPreferences
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _readingTheme = MutableStateFlow(ReadingTheme.WHITE)
    val readingTheme: StateFlow<ReadingTheme> = _readingTheme

    /** Sourced from the library's own PreferencesRepository (proves it persists). */
    lateinit var colorScheme: StateFlow<ColorSchemeType>
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _readingTheme.value = runCatching {
            ReadingTheme.valueOf(prefs.getString(KEY_READING_THEME, null) ?: "WHITE")
        }.getOrDefault(ReadingTheme.WHITE)

        colorScheme = MushafLibrary.getPreferencesRepository()
            .getThemeConfigFlow()
            .map { it.colorScheme.toUi() }
            .stateIn(scope, SharingStarted.Eagerly, ColorSchemeType.DEFAULT)
    }

    fun setReadingTheme(theme: ReadingTheme) {
        _readingTheme.value = theme
        prefs.edit().putString(KEY_READING_THEME, theme.name).apply()
    }

    private const val KEY_READING_THEME = "reading_theme"
}

fun ColorScheme.toUi(): ColorSchemeType = when (this) {
    ColorScheme.DEFAULT -> ColorSchemeType.DEFAULT
    ColorScheme.WARM -> ColorSchemeType.WARM
    ColorScheme.COOL -> ColorSchemeType.COOL
    ColorScheme.SEPIA -> ColorSchemeType.SEPIA
}

fun ColorSchemeType.toCore(): ColorScheme = when (this) {
    ColorSchemeType.DEFAULT -> ColorScheme.DEFAULT
    ColorSchemeType.WARM -> ColorScheme.WARM
    ColorSchemeType.COOL -> ColorScheme.COOL
    ColorSchemeType.SEPIA -> ColorScheme.SEPIA
}
