package com.yahiaragae.mushafimad.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the last read position in the Mushaf.
 * Provides methods to save, retrieve, and clear the last read position.
 */
class LastReadPositionManager(private val context: Context) {
    
    private companion object {
        private const val PREF_NAME = "last_read_position"
        private const val KEY_PAGE_NUMBER = "page_number"
        private const val KEY_AYAH_NUMBER = "ayah_number"
        private const val KEY_SURA_NUMBER = "sura_number"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Data class representing the last read position in the Mushaf
     */
    data class LastReadPosition(
        val pageNumber: Int,
        val suraNumber: Int,
        val ayahNumber: Int
    )
    
    /**
     * Saves the current reading position
     * @param position The position to save
     */
    fun saveLastReadPosition(position: LastReadPosition) {
        with(sharedPreferences.edit()) {
            putInt(KEY_PAGE_NUMBER, position.pageNumber)
            putInt(KEY_SURA_NUMBER, position.suraNumber)
            putInt(KEY_AYAH_NUMBER, position.ayahNumber)
            apply()
        }
    }
    
    /**
     * Retrieves the last saved reading position
     * @return The last read position or null if none exists
     */
    fun getLastReadPosition(): LastReadPosition? {
        val pageNumber = sharedPreferences.getInt(KEY_PAGE_NUMBER, -1)
        val suraNumber = sharedPreferences.getInt(KEY_SURA_NUMBER, -1)
        val ayahNumber = sharedPreferences.getInt(KEY_AYAH_NUMBER, -1)
        
        return if (pageNumber != -1 && suraNumber != -1 && ayahNumber != -1) {
            LastReadPosition(pageNumber, suraNumber, ayahNumber)
        } else {
            null
        }
    }
    
    /**
     * Clears the saved last read position
     */
    fun clearLastReadPosition() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Checks if there is a saved last read position
     * @return true if a position is saved, false otherwise
     */
    fun hasLastReadPosition(): Boolean {
        return getLastReadPosition() != null
    }
}