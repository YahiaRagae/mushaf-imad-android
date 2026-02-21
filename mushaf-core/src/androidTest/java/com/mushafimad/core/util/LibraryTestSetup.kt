package com.mushafimad.core.util

import androidx.test.platform.app.InstrumentationRegistry
import com.mushafimad.core.MushafLibrary

/**
 * Shared test setup helper for instrumented tests.
 *
 * MushafInitProvider auto-initializes the library via ContentProvider before any test code
 * runs. Calling initialize() explicitly from tests is safe (the implementation is idempotent),
 * and also validates TC-1.4 (no crash on double-init).
 */
internal object LibraryTestSetup {

    fun ensureInitialized() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        MushafLibrary.initialize(context)
    }
}
