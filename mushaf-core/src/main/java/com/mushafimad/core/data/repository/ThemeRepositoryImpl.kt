package com.mushafimad.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mushafimad.core.domain.models.ColorScheme
import com.mushafimad.core.domain.models.ThemeConfig
import com.mushafimad.core.domain.models.ThemeMode
import com.mushafimad.core.domain.repository.ThemeRepository
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

/**
 * Implementation of ThemeRepository using DataStore
 * Internal implementation - not exposed in public API
 */
internal class ThemeRepositoryImpl private constructor(
    private val context: Context
) : ThemeRepository {

    private val dataStore = context.themeDataStore

    companion object {
        @Volatile private var instance: ThemeRepositoryImpl? = null

        fun getInstance(): ThemeRepository = instance ?: synchronized(this) {
            instance ?: ThemeRepositoryImpl(
                ServiceRegistry.getContext()
            ).also { instance = it }
        }
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme")
        private val AMOLED_MODE_KEY = booleanPreferencesKey("amoled_mode")
    }

    override fun getThemeConfigFlow(): Flow<ThemeConfig> {
        return dataStore.data.map { preferences ->
            ThemeConfig(
                mode = preferences[THEME_MODE_KEY]?.let {
                    ThemeMode.valueOf(it)
                } ?: ThemeMode.SYSTEM,
                colorScheme = preferences[COLOR_SCHEME_KEY]?.let {
                    ColorScheme.valueOf(it)
                } ?: ColorScheme.DEFAULT,
                useAmoled = preferences[AMOLED_MODE_KEY] ?: false
            )
        }
    }

    override suspend fun getThemeConfig(): ThemeConfig {
        return getThemeConfigFlow().first()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    override suspend fun setColorScheme(scheme: ColorScheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME_KEY] = scheme.name
        }
    }

    override suspend fun setAmoledMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AMOLED_MODE_KEY] = enabled
        }
    }

    override suspend fun updateThemeConfig(config: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = config.mode.name
            preferences[COLOR_SCHEME_KEY] = config.colorScheme.name
            preferences[AMOLED_MODE_KEY] = config.useAmoled
        }
    }
}
