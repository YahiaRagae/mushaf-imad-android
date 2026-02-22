package com.mushafimad.sampleapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mushafimad.core.MushafLibrary
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryInitTest {

    @Test
    fun verifyManualInitializationAndIdempotency() {
         val context = InstrumentationRegistry.getInstrumentation().targetContext

         MushafLibrary.initialize(context)
        assertTrue("Library should be initialized manually", MushafLibrary.isInitialized())

         try {
            MushafLibrary.initialize(context)
            assertTrue("Library should still be initialized after second call", MushafLibrary.isInitialized())
        } catch (e: Exception) {
            org.junit.Assert.fail("Library crashed during second initialization: ${e.message}")
        }
    }
}