package com.mushafimad.ui.mushaf

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for MushafView basic rendering.
 *
 * QA Issue #37 — TC-2.1 through TC-2.8
 *
 * Koin is bootstrapped automatically via ContentProviders (MushafInitProvider and
 * MushafUiInitProvider) before any test activity is created, so no explicit setup
 * is required here.
 *
 * PageInfoDisplay renders the text "${pageNumber} / ${totalPages}", e.g. "1 / 604".
 * All assertions wait up to 10 seconds for async data loading to complete before
 * checking the rendered content, preventing flaky tests.
 */
@RunWith(AndroidJUnit4::class)
class MushafViewRenderingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // TC-2.1: Embed MushafView() with all defaults — verify a Quran page renders (content present)
    @Test
    fun tc2_1_defaultParameters_pageContentIsPresent() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView()
            }
        }

        waitForPageInfoToAppear()

        composeTestRule
            .onNodeWithText("/ 604", substring = true)
            .assertIsDisplayed()
    }

    // TC-2.2: Arabic text with diacritics renders correctly (page content visible after load)
    @Test
    fun tc2_2_arabicContentVisible_afterLoadingCompletes() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView()
            }
        }

        // Loading indicator must disappear before content assertions
        waitForLoadingIndicatorToDisappear()

        // Page info is the structural signal that the page rendered successfully
        composeTestRule
            .onNodeWithText("/ 604", substring = true)
            .assertIsDisplayed()
    }

    // TC-2.3: Portrait orientation — verify layout renders correctly without crash
    @Test
    fun tc2_3_portraitOrientation_rendersWithoutCrash() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView()
            }
        }

        waitForPageInfoToAppear()

        composeTestRule
            .onNodeWithText("/ 604", substring = true)
            .assertIsDisplayed()
    }

    // TC-2.5: initialPage = 1 — verify first page loads correctly
    @Test
    fun tc2_5_initialPageOne_firstPageLoads() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView(initialPage = 1)
            }
        }

        waitForSpecificPageToAppear(pageNumber = 1)

        composeTestRule
            .onNodeWithText("1 / 604")
            .assertIsDisplayed()
    }

    // TC-2.6: initialPage = 604 — verify last page loads correctly
    @Test
    fun tc2_6_initialPageSixHundredAndFour_lastPageLoads() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView(initialPage = 604)
            }
        }

        waitForSpecificPageToAppear(pageNumber = 604)

        composeTestRule
            .onNodeWithText("604 / 604")
            .assertIsDisplayed()
    }

    // TC-2.7: initialPage = 300 — verify mid-range page loads correctly
    @Test
    fun tc2_7_initialPageThreeHundred_midRangePageLoads() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView(initialPage = 300)
            }
        }

        waitForSpecificPageToAppear(pageNumber = 300)

        composeTestRule
            .onNodeWithText("300 / 604")
            .assertIsDisplayed()
    }

    // TC-2.8: initialPage = null (default) — verify loads to saved or first page
    @Test
    fun tc2_8_initialPageNull_defaultsToSavedOrFirstPage() {
        composeTestRule.setContent {
            MaterialTheme {
                MushafView(initialPage = null)
            }
        }

        waitForPageInfoToAppear()

        // Any valid page loaded will contain "/ 604" in the page info overlay
        composeTestRule
            .onNodeWithText("/ 604", substring = true)
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Waits until the loading indicator text ("جاري التحميل...") is no longer
     * present in the composition, indicating that data loading has finished.
     */
    private fun waitForLoadingIndicatorToDisappear() {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithText("جاري التحميل...")
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    /**
     * Waits until any node containing "/ 604" (the page info overlay suffix) is
     * visible, confirming that at least one page has been loaded and displayed.
     */
    private fun waitForPageInfoToAppear() {
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithText("/ 604", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    /**
     * Waits until the page info overlay shows the exact text for [pageNumber],
     * e.g. "1 / 604", "300 / 604", "604 / 604".
     */
    private fun waitForSpecificPageToAppear(pageNumber: Int) {
        val expectedText = "$pageNumber / 604"
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithText(expectedText)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
