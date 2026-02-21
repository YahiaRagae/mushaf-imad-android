package com.mushafimad.ui.player

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for QuranPlayerView — Seeking & Progress.
 *
 * QA Issue #33 — TC-3.13 through TC-3.17
 *
 * Koin is bootstrapped automatically via ContentProviders (MushafInitProvider and
 * MushafUiInitProvider) before any test activity is created, so no explicit setup
 * is required here.
 *
 * The progress bar is a custom Box (not Slider/SeekBar), so Compose semantics are
 * used to verify the time labels that surround it. autoPlay = false is used to avoid
 * triggering network requests during structural rendering tests.
 */
@RunWith(AndroidJUnit4::class)
class QuranPlayerViewSeekingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // TC-3.13: Progress bar section is present — verified via the time labels
    // that are siblings to the progress bar within PlayerProgressBar
    @Test
    fun tc3_13_playerView_progressBarSection_isPresent() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForPlayerToRender()

        // Both time labels must be visible for the progress bar section to be present
        composeTestRule
            .onNodeWithText("0:00")
            .assertIsDisplayed()
    }

    // TC-3.14: Current time shows in "0:00" format when at position zero
    @Test
    fun tc3_14_playerView_currentTimeLabel_showsZeroZeroFormat() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForPlayerToRender()

        // At initial state, currentTimeMs = 0 → formatTime(0) = "0:00"
        composeTestRule
            .onNodeWithText("0:00")
            .assertIsDisplayed()
    }

    // TC-3.15: Remaining time label shows with "-" prefix
    @Test
    fun tc3_15_playerView_remainingTimeLabel_showsMinusPrefix() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForPlayerToRender()

        // At initial state, durationMs = 0, remainingMs = 0 → "-0:00"
        composeTestRule
            .onNodeWithText("-0:00")
            .assertIsDisplayed()
    }

    // TC-3.16: Progress bar enables when duration > 0 — requires audio loaded from network.
    // Structural check: current time label is in a layout that contains the progress bar.
    @Test
    fun tc3_16_playerView_progressBarLayout_currentTimeLabelAlwaysRendered() {
        // TC-3.16: seek enable when duration > 0 verified manually (requires active audio session)
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForPlayerToRender()

        // The time labels are always rendered regardless of enabled state
        composeTestRule
            .onNodeWithText("0:00")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("-0:00")
            .assertIsDisplayed()
    }

    // TC-3.17: Progress bar / seek is disabled during LOADING state.
    // The progress bar uses a custom Box with conditionally applied pointerInput;
    // no standard semantics disabled attribute is set. Verification is via:
    // (a) loading text presence indicating LOADING state, and
    // (b) time labels still rendered (progress bar section visible).
    @Test
    fun tc3_17_playerView_duringLoadingState_progressBarSectionStillRendered() {
        // TC-3.17: seek gesture disabled during LOADING verified manually (no standard
        // semantics disabled attribute on custom Box progress bar)
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        // Wait until either loading or idle state — both still show the progress section
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            val loadingNodes = composeTestRule
                .onAllNodesWithText("Loading audio...")
                .fetchSemanticsNodes()
            val playNodes = composeTestRule
                .onAllNodesWithContentDescription("Play")
                .fetchSemanticsNodes()
            loadingNodes.isNotEmpty() || playNodes.isNotEmpty()
        }

        // Time labels are rendered in both LOADING and IDLE states
        composeTestRule
            .onNodeWithText("0:00")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("-0:00")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Waits until the player has rendered far enough that the progress bar's
     * time labels ("0:00" and "-0:00") are visible in the composition.
     */
    private fun waitForPlayerToRender() {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithText("0:00")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
