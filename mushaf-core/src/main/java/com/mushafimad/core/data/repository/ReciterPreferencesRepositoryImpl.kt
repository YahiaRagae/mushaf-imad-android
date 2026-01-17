package com.mushafimad.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mushafimad.core.domain.repository.ReciterPreferencesRepository
import com.mushafimad.core.internal.ServiceRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reciterDataStore by preferencesDataStore(name = "reciter_preferences")

/**
 * Implementation of ReciterPreferencesRepository using DataStore
 * Internal implementation - not exposed in public API
 */
internal class ReciterPreferencesRepositoryImpl private constructor(
    private val context: Context
) : ReciterPreferencesRepository {

    private val dataStore = context.reciterDataStore

    companion object {
        @Volatile private var instance: ReciterPreferencesRepositoryImpl? = null

        fun getInstance(): ReciterPreferencesRepository = instance ?: synchronized(this) {
            instance ?: ReciterPreferencesRepositoryImpl(
                ServiceRegistry.getContext()
            ).also { instance = it }
        }
        private val SELECTED_RECITER_ID_KEY = intPreferencesKey("selected_reciter_id")
        private val PLAYBACK_SPEED_KEY = floatPreferencesKey("playback_speed")
        private val REPEAT_MODE_KEY = booleanPreferencesKey("repeat_mode")

        private const val DEFAULT_RECITER_ID = 1 // Abdul Basit Abdul Samad
        private const val DEFAULT_PLAYBACK_SPEED = 1.0f
        private const val DEFAULT_REPEAT_MODE = false
    }

    override fun getSelectedReciterIdFlow(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[SELECTED_RECITER_ID_KEY] ?: DEFAULT_RECITER_ID
        }
    }

    override suspend fun getSelectedReciterId(): Int {
        return getSelectedReciterIdFlow().first()
    }

    override suspend fun setSelectedReciterId(reciterId: Int) {
        dataStore.edit { preferences ->
            preferences[SELECTED_RECITER_ID_KEY] = reciterId
        }
    }

    override fun getPlaybackSpeedFlow(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[PLAYBACK_SPEED_KEY] ?: DEFAULT_PLAYBACK_SPEED
        }
    }

    override suspend fun getPlaybackSpeed(): Float {
        return getPlaybackSpeedFlow().first()
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED_KEY] = clampedSpeed
        }
    }

    override fun getRepeatModeFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[REPEAT_MODE_KEY] ?: DEFAULT_REPEAT_MODE
        }
    }

    override suspend fun getRepeatMode(): Boolean {
        return getRepeatModeFlow().first()
    }

    override suspend fun setRepeatMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REPEAT_MODE_KEY] = enabled
        }
    }
}
