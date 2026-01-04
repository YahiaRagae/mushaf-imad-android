package com.mushafimad.library

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MushafLibraryTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        // Reset library state before each test using reflection
        resetLibraryState()
    }

    @Test
    fun `library initializes successfully`() {
        MushafLibrary.initialize(context)

        assertTrue(MushafLibrary.isInitialized())
    }

    @Test
    fun `library initialization is idempotent`() {
        MushafLibrary.initialize(context)
        MushafLibrary.initialize(context) // Should not throw

        assertTrue(MushafLibrary.isInitialized())
    }

    @Test(expected = IllegalStateException::class)
    fun `getContext throws exception when not initialized`() {
        MushafLibrary.getContext()
    }

    @Test
    fun `getContext returns context after initialization`() {
        MushafLibrary.initialize(context)

        assertNotNull(MushafLibrary.getContext())
    }

    private fun resetLibraryState() {
        // Reset the isInitialized flag using reflection for testing
        try {
            val field = MushafLibrary::class.java.getDeclaredField("isInitialized")
            field.isAccessible = true
            field.setBoolean(MushafLibrary, false)

            val contextField = MushafLibrary::class.java.getDeclaredField("applicationContext")
            contextField.isAccessible = true
            contextField.set(MushafLibrary, null)
        } catch (e: Exception) {
            // Ignore if reflection fails
        }
    }
}
