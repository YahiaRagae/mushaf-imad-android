package com.mushafimad.core.util

import androidx.test.platform.app.InstrumentationRegistry
import com.mushafimad.core.MushafLibrary

/**
 * Utility object that ensures the Mushaf library is initialized before instrumented tests run.
 *
 * The library auto-initializes via [com.mushafimad.core.internal.MushafInitProvider] (a
 * ContentProvider) when the test process starts on the device. [ensureInitialized] is therefore
 * a lightweight guard: if the ContentProvider has already run, it is a no-op; if for any reason
 * it has not, it triggers manual initialization so tests are not flaky.
 */
object LibraryTestSetup {

    /**
     * Ensures the library is initialized. Safe to call multiple times — idempotent.
     */
    fun ensureInitialized() {
        if (!MushafLibrary.isInitialized()) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            MushafLibrary.initialize(context)
        }
    }
}
