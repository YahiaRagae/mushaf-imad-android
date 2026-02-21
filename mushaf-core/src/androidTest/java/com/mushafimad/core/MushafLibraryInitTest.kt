package com.mushafimad.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.util.LibraryTestSetup
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for MushafLibrary initialization and repository access.
 *
 * Covers: QA-1.2 (Manual Initialization) and QA-1.3 (Repository Access)
 * Issues: #42, #43
 *
 * TC-1.3:  Manual initialize() succeeds and returns isInitialized() = true
 * TC-1.4:  Calling initialize() a second time is idempotent (no crash)
 * TC-1.5 – TC-1.14: All repository getters return non-null singletons
 */
@RunWith(AndroidJUnit4::class)
class MushafLibraryInitTest {

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
    }

    // ──────────────────────────── TC-1.3 ────────────────────────────

    @Test
    fun initialize_withApplicationContext_isInitializedReturnsTrue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        MushafLibrary.initialize(context)

        assertThat(MushafLibrary.isInitialized()).isTrue()
    }

    // ──────────────────────────── TC-1.4 ────────────────────────────

    @Test
    fun initialize_calledTwice_doesNotCrash() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // First call (may already be initialized via ContentProvider)
        MushafLibrary.initialize(context)
        // Second call – must be idempotent
        MushafLibrary.initialize(context)

        assertThat(MushafLibrary.isInitialized()).isTrue()
    }

    // ──────────────────────────── TC-1.5 – TC-1.14 ────────────────────────────

    @Test
    fun getQuranRepository_returnsNonNull() {
        assertThat(MushafLibrary.getQuranRepository()).isNotNull()
    }

    @Test
    fun getChapterRepository_returnsNonNull() {
        assertThat(MushafLibrary.getChapterRepository()).isNotNull()
    }

    @Test
    fun getPageRepository_returnsNonNull() {
        assertThat(MushafLibrary.getPageRepository()).isNotNull()
    }

    @Test
    fun getVerseRepository_returnsNonNull() {
        assertThat(MushafLibrary.getVerseRepository()).isNotNull()
    }

    @Test
    fun getAudioRepository_returnsNonNull() {
        assertThat(MushafLibrary.getAudioRepository()).isNotNull()
    }

    @Test
    fun getBookmarkRepository_returnsNonNull() {
        assertThat(MushafLibrary.getBookmarkRepository()).isNotNull()
    }

    @Test
    fun getReadingHistoryRepository_returnsNonNull() {
        assertThat(MushafLibrary.getReadingHistoryRepository()).isNotNull()
    }

    @Test
    fun getSearchHistoryRepository_returnsNonNull() {
        assertThat(MushafLibrary.getSearchHistoryRepository()).isNotNull()
    }

    @Test
    fun getPreferencesRepository_returnsNonNull() {
        assertThat(MushafLibrary.getPreferencesRepository()).isNotNull()
    }

    @Test
    fun getDataExportRepository_returnsNonNull() {
        assertThat(MushafLibrary.getDataExportRepository()).isNotNull()
    }

    // ──────────────────────────── Singleton contract ────────────────────────────

    @Test
    fun getChapterRepository_returnsSameInstanceOnMultipleCalls() {
        val first = MushafLibrary.getChapterRepository()
        val second = MushafLibrary.getChapterRepository()
        assertThat(first).isSameInstanceAs(second)
    }
}
