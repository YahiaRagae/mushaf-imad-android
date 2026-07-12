package com.mushafimad.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Regression test for the zero-configuration claim.
 *
 * v0.1 called startKoin on the global context from its ContentProvider, so a
 * host app that used Koin itself crashed with "KoinApplication already
 * started" (QA issue: "App crashes due to missing Koin configuration"). The
 * library now runs an isolated Koin context.
 */
@RunWith(AndroidJUnit4::class)
class KoinIsolationTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun hostAppStartsItsOwnGlobalKoin_libraryStillWorks() = runBlocking {
        // MushafInitProvider has already auto-initialized the library.
        assertThat(MushafLibrary.isInitialized()).isTrue()

        // A host app claims the global Koin context with its own modules.
        // With v0.1 this threw; with the isolated context both coexist.
        val hostKoin = startKoin {
            modules(module { single { "host-app-dependency" } })
        }.koin

        // Host resolution works...
        assertThat(hostKoin.get<String>()).isEqualTo("host-app-dependency")
        // ...and the host context does NOT contain library internals.
        assertThat(hostKoin.getOrNull<com.mushafimad.core.domain.repository.ChapterRepository>()).isNull()

        // Library resolution still works from its own context.
        val chapters = MushafLibrary.getChapterRepository().getAllChapters()
        assertThat(chapters).hasSize(114)
    }
}
