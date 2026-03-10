package com.yahiaragae.mushafimad.core

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class LastReadPositionManagerTest {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var lastReadPositionManager: LastReadPositionManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        lastReadPositionManager = LastReadPositionManager(context)
        
        // Clear any existing preferences before each test
        val editor = context.getSharedPreferences("last_read_position", Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }
    
    @Test
    fun `saveLastReadPosition saves position correctly`() {
        val position = LastReadPositionManager.LastReadPosition(
            pageNumber = 10,
            suraNumber = 2,
            ayahNumber = 5
        )
        
        lastReadPositionManager.saveLastReadPosition(position)
        
        val savedPosition = lastReadPositionManager.getLastReadPosition()
        assertNotNull(savedPosition)
        assertEquals(10, savedPosition?.pageNumber)
        assertEquals(2, savedPosition?.suraNumber)
        assertEquals(5, savedPosition?.ayahNumber)
    }
    
    @Test
    fun `getLastReadPosition returns null when no position is saved`() {
        val position = lastReadPositionManager.getLastReadPosition()
        assertNull(position)
    }
    
    @Test
    fun `clearLastReadPosition clears saved position`() {
        val position = LastReadPositionManager.LastReadPosition(
            pageNumber = 15,
            suraNumber = 3,
            ayahNumber = 8
        )
        
        lastReadPositionManager.saveLastReadPosition(position)
        assertNotNull(lastReadPositionManager.getLastReadPosition())
        
        lastReadPositionManager.clearLastReadPosition()
        assertNull(lastReadPositionManager.getLastReadPosition())
    }
    
    @Test
    fun `hasLastReadPosition returns true when position is saved`() {
        val position = LastReadPositionManager.LastReadPosition(
            pageNumber = 20,
            suraNumber = 4,
            ayahNumber = 12
        )
        
        lastReadPositionManager.saveLastReadPosition(position)
        assertTrue(lastReadPositionManager.hasLastReadPosition())
    }
    
    @Test
    fun `hasLastReadPosition returns false when no position is saved`() {
        assertFalse(lastReadPositionManager.hasLastReadPosition())
    }
    
    @Test
    fun `hasLastReadPosition returns false after clearing position`() {
        val position = LastReadPositionManager.LastReadPosition(
            pageNumber = 25,
            suraNumber = 5,
            ayahNumber = 15
        )
        
        lastReadPositionManager.saveLastReadPosition(position)
        assertTrue(lastReadPositionManager.hasLastReadPosition())
        
        lastReadPositionManager.clearLastReadPosition()
        assertFalse(lastReadPositionManager.hasLastReadPosition())
    }
    
    @Test
    fun `saving different positions updates the stored value`() {
        val firstPosition = LastReadPositionManager.LastReadPosition(
            pageNumber = 1,
            suraNumber = 1,
            ayahNumber = 1
        )
        
        val secondPosition = LastReadPositionManager.LastReadPosition(
            pageNumber = 100,
            suraNumber = 114,
            ayahNumber = 6
        )
        
        lastReadPositionManager.saveLastReadPosition(firstPosition)
        var currentPosition = lastReadPositionManager.getLastReadPosition()
        assertEquals(1, currentPosition?.pageNumber)
        
        lastReadPositionManager.saveLastReadPosition(secondPosition)
        currentPosition = lastReadPositionManager.getLastReadPosition()
        assertEquals(100, currentPosition?.pageNumber)
        assertEquals(114, currentPosition?.suraNumber)
        assertEquals(6, currentPosition?.ayahNumber)
    }
}