package com.mushafimad.library.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mushaf_preferences")

/**
 * Implementation of PreferencesRepository using DataStore
 * Internal API - not exposed to library consumers
 */
@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(
    private val context: Context
) : PreferencesRepository {

    private val dataStore = context.dataStore

    companion object {
        private val MUSHAF_TYPE_KEY = stringPreferencesKey("mushaf_type")
        private val CURRENT_PAGE_KEY = intPreferencesKey("current_page")
        private val LAST_READ_CHAPTER_KEY = intPreferencesKey("last_read_chapter")
        private val LAST_READ_CHAPTER_NUMBER_KEY = intPreferencesKey("last_read_chapter_number")
        private val LAST_READ_VERSE_NUMBER_KEY = intPreferencesKey("last_read_verse_number")
        private val FONT_SIZE_MULTIPLIER_KEY = floatPreferencesKey("font_size_multiplier")
        private val SHOW_TRANSLATION_KEY = booleanPreferencesKey("show_translation")

        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_FONT_SIZE_MULTIPLIER = 1.0f
    }

    override fun getMushafTypeFlow(): Flow<MushafType> = dataStore.data.map { preferences ->
        val typeString = preferences[MUSHAF_TYPE_KEY] ?: MushafType.HAFS_1441.name
        try {
            MushafType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            MushafType.HAFS_1441
        }
    }

    override suspend fun setMushafType(mushafType: MushafType) {
        dataStore.edit { preferences ->
            preferences[MUSHAF_TYPE_KEY] = mushafType.name
        }
    }

    override fun getCurrentPageFlow(): Flow<Int> = dataStore.data.map { preferences ->
        preferences[CURRENT_PAGE_KEY] ?: DEFAULT_PAGE
    }

    override suspend fun setCurrentPage(pageNumber: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENT_PAGE_KEY] = pageNumber
        }
    }

    override fun getLastReadChapterFlow(): Flow<Int?> = dataStore.data.map { preferences ->
        preferences[LAST_READ_CHAPTER_KEY]
    }

    override suspend fun setLastReadChapter(chapterNumber: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_READ_CHAPTER_KEY] = chapterNumber
        }
    }

    override fun getLastReadVerseFlow(): Flow<Pair<Int, Int>?> = dataStore.data.map { preferences ->
        val chapterNumber = preferences[LAST_READ_CHAPTER_NUMBER_KEY]
        val verseNumber = preferences[LAST_READ_VERSE_NUMBER_KEY]

        if (chapterNumber != null && verseNumber != null) {
            Pair(chapterNumber, verseNumber)
        } else {
            null
        }
    }

    override suspend fun setLastReadVerse(chapterNumber: Int, verseNumber: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_READ_CHAPTER_NUMBER_KEY] = chapterNumber
            preferences[LAST_READ_VERSE_NUMBER_KEY] = verseNumber
        }
    }

    override fun getFontSizeMultiplierFlow(): Flow<Float> = dataStore.data.map { preferences ->
        preferences[FONT_SIZE_MULTIPLIER_KEY] ?: DEFAULT_FONT_SIZE_MULTIPLIER
    }

    override suspend fun setFontSizeMultiplier(multiplier: Float) {
        dataStore.edit { preferences ->
            // Clamp between 0.5 and 2.0
            val clamped = multiplier.coerceIn(0.5f, 2.0f)
            preferences[FONT_SIZE_MULTIPLIER_KEY] = clamped
        }
    }

    override fun getShowTranslationFlow(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_TRANSLATION_KEY] ?: false
    }

    override suspend fun setShowTranslation(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_TRANSLATION_KEY] = show
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
