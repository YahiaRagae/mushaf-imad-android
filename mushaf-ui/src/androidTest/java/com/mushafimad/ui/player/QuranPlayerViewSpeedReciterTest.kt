package com.mushafimad.ui.player

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for QuranPlayerView — Speed & Reciter.
 *
 * QA Issue #34 — TC-3.18 through TC-3.28
 *
 * Koin is bootstrapped automatically via ContentProviders (MushafInitProvider and
 * MushafUiInitProvider) before any test activity is created, so no explicit setup
 * is required here.
 *
 * The speed control is an IconButton containing a Text (no content description).
 * The repeat button has content description "Repeat".
 * The reciter name in the header is clickable and opens ReciterPickerDialog.
 * autoPlay = false is used to avoid triggering network requests.
 */
@RunWith(AndroidJUnit4::class)
class QuranPlayerViewSpeedReciterTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // TC-3.18: Speed control button is present — verified via "1x" text at default rate
    @Test
    fun tc3_18_playerView_speedButton_defaultRateTextPresent() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        // Default playback rate is 1.0f → formatPlaybackRate(1.0f) = "1x"
        composeTestRule
            .onNodeWithText("1x")
            .assertIsDisplayed()
    }

    // TC-3.19: Speed button shows current rate text ("1x" at initial state)
    @Test
    fun tc3_19_playerView_speedButton_showsRateText() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        composeTestRule
            .onNodeWithText("1x")
            .assertIsDisplayed()
    }

    // TC-3.20: Speed button cycles to next rate on click
    @Test
    fun tc3_20_playerView_speedButton_onClickCyclesToNextRate() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        // Confirm starting state is "1x"
        composeTestRule
            .onNodeWithText("1x")
            .assertIsDisplayed()

        // Click speed button — PLAYBACK_RATES = [0.75, 1.0, 1.25, 1.5, ...], index of 1.0 = 1
        // Next rate is 1.25f → formatPlaybackRate(1.25f) = "1.25x"
        composeTestRule
            .onNodeWithText("1x")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithText("0.75x")
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("1.25x")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // After cycling from 1x, the next rate in the list is 1.25x
        composeTestRule
            .onNodeWithText("1.25x")
            .assertIsDisplayed()
    }

    // TC-3.21: Speed button text updates after cycling through multiple rates
    @Test
    fun tc3_21_playerView_speedButton_textUpdatesOnEachCycle() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        composeTestRule
            .onNodeWithText("1x")
            .assertIsDisplayed()

        // First click: 1.0 → 1.25
        composeTestRule.onNodeWithText("1x").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithText("1.25x").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("1.25x").assertIsDisplayed()

        // Second click: 1.25 → 1.5
        composeTestRule.onNodeWithText("1.25x").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithText("1.5x").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("1.5x").assertIsDisplayed()
    }

    // TC-3.22: Repeat button is present with content description "Repeat"
    @Test
    fun tc3_22_playerView_repeatButton_isPresent() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        composeTestRule
            .onNodeWithContentDescription("Repeat")
            .assertIsDisplayed()
    }

    // TC-3.23: Repeat button is clickable and can be toggled
    @Test
    fun tc3_23_playerView_repeatButton_isClickable() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForControlsToRender()

        composeTestRule
            .onNodeWithContentDescription("Repeat")
            .assertIsDisplayed()
            .performClick()

        // After click, repeat button still exists (toggle — no crash or disappearance)
        composeTestRule
            .onNodeWithContentDescription("Repeat")
            .assertIsDisplayed()
    }

    // TC-3.24: Reciter name is visible in the header
    @Test
    fun tc3_24_playerView_header_reciterNameIsDisplayed() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        // Wait until the reciter is loaded and its name appears in the header
        waitForReciterNameToAppear()

        // "Select reciter" arrow icon marks the reciter row; the reciter name text is adjacent
        composeTestRule
            .onNodeWithContentDescription("Select reciter")
            .assertIsDisplayed()
    }

    // TC-3.25: Clicking "Select reciter" arrow / reciter name row opens ReciterPickerDialog
    @Test
    fun tc3_25_playerView_clickingReciterRow_opensReciterPickerDialog() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForReciterNameToAppear()

        composeTestRule
            .onNodeWithContentDescription("Select reciter")
            .assertIsDisplayed()
            .performClick()

        // ReciterPickerDialog header text
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithText("Select Reciter")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Select Reciter")
            .assertIsDisplayed()
    }

    // TC-3.26: ReciterPickerDialog contains the Cancel button
    @Test
    fun tc3_26_playerView_reciterPickerDialog_containsCancelButton() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    autoPlay = false
                )
            }
        }

        waitForReciterNameToAppear()

        composeTestRule
            .onNodeWithContentDescription("Select reciter")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithText("Cancel")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
    }

    // TC-3.27: reciterId parameter is accepted — player renders without crash
    @Test
    fun tc3_27_playerView_withReciterId_rendersWithoutCrash() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    reciterId = 1,
                    autoPlay = false
                )
            }
        }

        composeTestRule
            .onNodeWithText("الفاتحة")
            .assertIsDisplayed()
    }

    // TC-3.28: reciterId = null (default) — player renders with default reciter
    @Test
    fun tc3_28_playerView_withNoReciterId_rendersWithDefaultReciter() {
        composeTestRule.setContent {
            MaterialTheme {
                QuranPlayerView(
                    chapterNumber = 1,
                    chapterName = "الفاتحة",
                    reciterId = null,
                    autoPlay = false
                )
            }
        }

        composeTestRule
            .onNodeWithText("الفاتحة")
            .assertIsDisplayed()

        // "Select reciter" arrow indicates the reciter row is rendered
        waitForReciterNameToAppear()

        composeTestRule
            .onNodeWithContentDescription("Select reciter")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Waits until the controls row has rendered, detected via the "Repeat" button
     * or the "Play" button appearing in the composition.
     */
    private fun waitForControlsToRender() {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            val repeatNodes = composeTestRule
                .onAllNodesWithContentDescription("Repeat")
                .fetchSemanticsNodes()
            val playNodes = composeTestRule
                .onAllNodesWithContentDescription("Play")
                .fetchSemanticsNodes()
            repeatNodes.isNotEmpty() || playNodes.isNotEmpty()
        }
    }

    /**
     * Waits until the reciter row in the header is present, detected via the
     * "Select reciter" content description on the arrow icon.
     */
    private fun waitForReciterNameToAppear() {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithContentDescription("Select reciter")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
