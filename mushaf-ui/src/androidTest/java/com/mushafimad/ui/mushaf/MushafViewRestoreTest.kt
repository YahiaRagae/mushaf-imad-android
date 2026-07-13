package com.mushafimad.ui.mushaf

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression tests for QA issue #40 ("reading position not restored after
 * reopening the app").
 *
 * v0.1 could never restore a position: bookmarks/history/last-position lived in
 * quran.realm, which was deleted and recopied from assets on every launch. The
 * position now lives in a separate userdata.realm that is never deleted.
 *
 * These tests drive the real MushafView composable, so they also pin down the
 * documented contract of `initialPage`:
 *   - initialPage == null  -> resume the saved position
 *   - initialPage != null  -> the caller wins, saved position is ignored
 */
private const val NEXT_PAGE = "الصفحة التالية"

@RunWith(AndroidJUnit4::class)
class MushafViewRestoreTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val readingHistory = MushafLibrary.getReadingHistoryRepository()

    private fun savePosition(page: Int, chapter: Int) = runBlocking {
        readingHistory.updateLastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = chapter,
            verseNumber = 1,
            pageNumber = page,
            scrollPosition = 0f
        )
    }

    @Test
    fun savedPositionSurvivesAndIsReadableFromTheRepository() = runBlocking {
        savePosition(page = 250, chapter = 18)

        val restored = readingHistory.getLastReadPosition(MushafType.HAFS_1441)

        assertThat(restored).isNotNull()
        assertThat(restored!!.pageNumber).isEqualTo(250)
        assertThat(restored.chapterNumber).isEqualTo(18)
    }

    @Test
    fun nullInitialPage_restoresSavedPosition() {
        savePosition(page = 250, chapter = 18)

        var pageShown: Int? = null
        compose.setContent {
            MushafView(
                initialPage = null,
                onPageChanged = { pageShown = it }
            )
        }

        // The reader opens on the saved page, not on page 1
        compose.waitUntil(timeoutMillis = 15_000) { pageShown == 250 }
        assertThat(pageShown).isEqualTo(250)
    }

    @Test
    fun explicitInitialPage_overridesSavedPosition() {
        savePosition(page = 250, chapter = 18)

        var pageShown: Int? = null
        compose.setContent {
            MushafView(
                initialPage = 5,
                onPageChanged = { pageShown = it }
            )
        }

        // The caller asked for page 5, so page 5 wins over the saved 250
        compose.waitUntil(timeoutMillis = 15_000) { pageShown == 5 }
        assertThat(pageShown).isEqualTo(5)
    }

    @Test
    fun mushafWithPlayerView_nullInitialPage_alsoRestoresSavedPosition() {
        savePosition(page = 250, chapter = 18)

        var pageShown: Int? = null
        compose.setContent {
            MushafWithPlayerView(
                initialPage = null,
                onPageChanged = { pageShown = it }
            )
        }

        // MushafWithPlayerView forwards initialPage to MushafView, so the
        // reader-with-audio screen resumes exactly like the plain reader
        compose.waitUntil(timeoutMillis = 15_000) { pageShown == 250 }
        assertThat(pageShown).isEqualTo(250)
    }

    @Test
    fun navigatingWritesTheNewPosition() {
        savePosition(page = 100, chapter = 5)

        var pageShown: Int? = null
        compose.setContent {
            MushafView(
                initialPage = null,
                onPageChanged = { pageShown = it }
            )
        }
        compose.waitUntil(timeoutMillis = 15_000) { pageShown == 100 }

        // onPageChanged fires when the pager settles on the page, which is not the same
        // moment the navigation overlay is laid out on top of it. On a slow emulator the
        // gap is wide enough to lose a race: the button is in the tree but not yet
        // placed, so it is not clickable and not hittable. That is what CI kept failing
        // on - first as "Failed to inject touch input" (performClick resolved a hit
        // point outside the window), then, once the assertion was explicit, as the
        // honest "The component is not displayed!".
        //
        // So wait for the button to actually be there. The assertion is not dropped - it
        // must become displayed within the timeout or the test still fails - it just
        // stops being read at an arbitrary instant.
        // isPlaced, not merely present: the button enters the semantics tree before the
        // overlay is laid out, and "in the tree but not placed" is exactly the state that
        // was failing. Waiting for existence alone would keep the race open.
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithContentDescription(NEXT_PAGE)
                .fetchSemanticsNodes()
                .any { it.layoutInfo.isPlaced }
        }

        // Then drive the click through the semantics action rather than by injecting a
        // touch event. What this test is about is that navigating persists the new
        // position; the emulator's input stack is not the thing under test. The checks
        // that the button is displayed and clickable are kept, explicitly.
        compose.onNodeWithContentDescription(NEXT_PAGE)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performSemanticsAction(SemanticsActions.OnClick)
        compose.waitUntil(timeoutMillis = 15_000) { pageShown == 101 }
        compose.waitUntil(timeoutMillis = 15_000) {
            runBlocking {
                readingHistory.getLastReadPosition(MushafType.HAFS_1441)?.pageNumber == 101
            }
        }

        val persisted = runBlocking { readingHistory.getLastReadPosition(MushafType.HAFS_1441) }
        assertThat(persisted!!.pageNumber).isEqualTo(101)
    }
}
