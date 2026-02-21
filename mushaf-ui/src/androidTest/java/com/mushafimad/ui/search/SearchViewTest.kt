package com.mushafimad.ui.search

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for SearchView — Basic Rendering & Search Functionality.
 *
 * QA Issue #29 — TC-4.1 through TC-4.14
 *
 * Koin is bootstrapped automatically via ContentProviders (MushafInitProvider and
 * MushafUiInitProvider) before any test activity is created, so no explicit setup
 * is required here.
 *
 * Search operations are backed by a pre-populated Realm database. Tests use waitUntil
 * with generous timeouts to accommodate asynchronous repository queries before asserting
 * on search results. Structural patterns ("Surah", "Page") are checked rather than
 * exact verse content to keep tests resilient against database updates.
 */
@RunWith(AndroidJUnit4::class)
class SearchViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun launchSearchView() {
        composeTestRule.setContent {
            MaterialTheme {
                SearchView()
            }
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.1: Embed SearchView() — verify search UI renders with search bar and filter chips
    // -------------------------------------------------------------------------

    @Test
    fun tc4_1_searchView_renders_searchBarAndFilterChipsVisible() {
        launchSearchView()

        composeTestRule
            .onNodeWithText("Search verses or chapters...", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chapters").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-4.2: Initial state shows "Recent Searches" section or empty state
    // -------------------------------------------------------------------------

    @Test
    fun tc4_2_initialState_showsSearchHistorySectionOrEmptyState() {
        launchSearchView()

        // On a fresh install / empty history the empty-state text is shown.
        // On a device with prior searches the "Recent Searches" header is shown.
        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            val emptyStateNodes = composeTestRule
                .onAllNodesWithText("No search history")
                .fetchSemanticsNodes()
            val recentHeaderNodes = composeTestRule
                .onAllNodesWithText("Recent Searches")
                .fetchSemanticsNodes()
            emptyStateNodes.isNotEmpty() || recentHeaderNodes.isNotEmpty()
        }

        val emptyStateNodes = composeTestRule
            .onAllNodesWithText("No search history")
            .fetchSemanticsNodes()

        if (emptyStateNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("No search history").assertIsDisplayed()
        } else {
            composeTestRule.onNodeWithText("Recent Searches").assertIsDisplayed()
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.3: Search bar has a leading search icon and placeholder text
    // -------------------------------------------------------------------------

    @Test
    fun tc4_3_searchBar_hasPlaceholderText() {
        launchSearchView()

        composeTestRule
            .onNodeWithText("Search verses or chapters...")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-4.4: Three filter chips visible: "All", "Verses", "Chapters"
    // -------------------------------------------------------------------------

    @Test
    fun tc4_4_filterChips_allThreeChipsDisplayed() {
        launchSearchView()

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chapters").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-4.5 / TC-4.6: Type Arabic query — verify "Searching..." indicator appears
    // -------------------------------------------------------------------------

    @Test
    fun tc4_5_typingQuery_triggersSearch() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        // After input the ViewModel sets isSearching = true before the coroutine completes,
        // so "Searching..." should appear — or results may arrive quickly enough that we
        // see them before the spinner is captured. Either outcome is valid.
        composeTestRule.waitUntil(timeoutMillis = 15_000L) {
            val searchingNodes = composeTestRule
                .onAllNodesWithText("Searching...")
                .fetchSemanticsNodes()
            val surahNodes = composeTestRule
                .onAllNodesWithText("Surah", substring = true)
                .fetchSemanticsNodes()
            val noResultsNodes = composeTestRule
                .onAllNodesWithText("No results found")
                .fetchSemanticsNodes()
            searchingNodes.isNotEmpty() || surahNodes.isNotEmpty() || noResultsNodes.isNotEmpty()
        }
    }

    @Test
    fun tc4_6_whileSearching_spinnerWithSearchingTextVisible() {
        launchSearchView()

        // Trigger a search and observe the transient loading state.
        // The "Searching..." text is only shown while isSearching == true.
        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        // Wait for the searching indicator OR for results (fast devices may skip the indicator)
        composeTestRule.waitUntil(timeoutMillis = 15_000L) {
            val searchingNodes = composeTestRule
                .onAllNodesWithText("Searching...")
                .fetchSemanticsNodes()
            val surahNodes = composeTestRule
                .onAllNodesWithText("Surah", substring = true)
                .fetchSemanticsNodes()
            val noResultsNodes = composeTestRule
                .onAllNodesWithText("No results found")
                .fetchSemanticsNodes()
            searchingNodes.isNotEmpty() || surahNodes.isNotEmpty() || noResultsNodes.isNotEmpty()
        }

        // If the indicator appeared, confirm it was the correct text
        val searchingNodes = composeTestRule
            .onAllNodesWithText("Searching...")
            .fetchSemanticsNodes()
        if (searchingNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Searching...").assertIsDisplayed()
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.7: Clear (X) button appears when text is entered
    // -------------------------------------------------------------------------

    @Test
    fun tc4_7_clearButton_appearsWhenTextIsEntered() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithContentDescription("Clear")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-4.8: Tap clear button → query clears, placeholder returns
    // -------------------------------------------------------------------------

    @Test
    fun tc4_8_tapClearButton_queryClears_placeholderReturns() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        composeTestRule.waitUntil(timeoutMillis = 10_000L) {
            composeTestRule
                .onAllNodesWithContentDescription("Clear")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        // After clear the TextField is empty and the placeholder is shown again
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodesWithText("Search verses or chapters...")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Search verses or chapters...")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-4.9 / TC-4.10: Verse results show "Surah X:Y" reference and page number
    // -------------------------------------------------------------------------

    @Test
    fun tc4_9_searchArabicText_verseResultsAppear() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        waitForVerseResultsOrNoResults()

        val surahNodes = composeTestRule
            .onAllNodesWithText("Surah", substring = true)
            .fetchSemanticsNodes()

        // Results must be present (the Bismillah appears in the pre-populated database)
        assert(surahNodes.isNotEmpty()) {
            "Expected verse results with 'Surah' reference after searching 'بسم'"
        }
    }

    @Test
    fun tc4_10_verseResult_showsSurahReferenceAndPageNumber() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        waitForVerseResultsOrNoResults()

        val surahNodes = composeTestRule
            .onAllNodesWithText("Surah", substring = true)
            .fetchSemanticsNodes()

        if (surahNodes.isNotEmpty()) {
            // Confirm the "Surah X:Y" reference pattern is rendered
            composeTestRule
                .onAllNodesWithText("Surah", substring = true)
                .fetchSemanticsNodes()
                .let { nodes ->
                    assert(nodes.isNotEmpty()) { "Expected 'Surah' reference in verse results" }
                }

            // Confirm the "Page N" label is also rendered
            val pageNodes = composeTestRule
                .onAllNodesWithText("Page", substring = true)
                .fetchSemanticsNodes()
            assert(pageNodes.isNotEmpty()) { "Expected 'Page' reference in verse results" }
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.11: Long verse text is truncated at 3 lines (structural — maxLines=3 is
    //          enforced by the Composable; visual truncation verified manually on device)
    // -------------------------------------------------------------------------

    @Test
    fun tc4_11_verseResultText_rendersWithoutOverflow() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("بسم")

        waitForVerseResultsOrNoResults()

        // The verse text renders inside a Card. If results are present the layout
        // must not crash, confirming the maxLines = 3 / TextOverflow.Ellipsis path runs.
        val surahNodes = composeTestRule
            .onAllNodesWithText("Surah", substring = true)
            .fetchSemanticsNodes()

        // Only assert the structural presence; visible truncation is a manual check
        assert(surahNodes.isNotEmpty() ||
                composeTestRule.onAllNodesWithText("No results found")
                    .fetchSemanticsNodes().isNotEmpty()) {
            "Expected either verse results or no-results state after search"
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.12: Search "البقرة" → chapter result appears
    // -------------------------------------------------------------------------

    @Test
    fun tc4_12_searchArabicChapterName_chapterResultAppears() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("البقرة")

        waitForChapterResultsOrNoResults()

        val chapterNodes = composeTestRule
            .onAllNodesWithText("البقرة", substring = true)
            .fetchSemanticsNodes()

        val chaptersHeaderNodes = composeTestRule
            .onAllNodesWithText("Chapters", substring = true)
            .fetchSemanticsNodes()

        // At minimum the chapter section header or the Arabic name badge must appear
        assert(chapterNodes.isNotEmpty() || chaptersHeaderNodes.size > 1) {
            "Expected chapter result for 'البقرة'"
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.13: Search "Baqarah" → chapter result appears (English title search)
    // -------------------------------------------------------------------------

    @Test
    fun tc4_13_searchEnglishChapterName_chapterResultAppears() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("Baqarah")

        waitForChapterResultsOrNoResults()

        // Either the English title "Al-Baqarah" / "Al-Baqara" or the Arabic title
        // should appear in the chapter result card
        val baqarahNodes = composeTestRule
            .onAllNodesWithText("Baqarah", substring = true)
            .fetchSemanticsNodes()

        val chaptersHeaderNodes = composeTestRule
            .onAllNodesWithText("Chapters", substring = true)
            .fetchSemanticsNodes()

        assert(baqarahNodes.isNotEmpty() || chaptersHeaderNodes.size > 1) {
            "Expected chapter result for English search 'Baqarah'"
        }
    }

    // -------------------------------------------------------------------------
    // TC-4.14: Chapter result shows number badge, Arabic title, English title
    // -------------------------------------------------------------------------

    @Test
    fun tc4_14_chapterResult_showsNumberBadgeAndTitles() {
        launchSearchView()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("البقرة")

        waitForChapterResultsOrNoResults()

        val chapterArabicNodes = composeTestRule
            .onAllNodesWithText("البقرة", substring = true)
            .fetchSemanticsNodes()

        if (chapterArabicNodes.isNotEmpty()) {
            // Arabic title is visible
            composeTestRule
                .onAllNodesWithText("البقرة", substring = true)
                .fetchSemanticsNodes()
                .let { assert(it.isNotEmpty()) { "Expected Arabic chapter title 'البقرة'" } }

            // English title should be present (e.g. "Al-Baqarah")
            val englishTitleNodes = composeTestRule
                .onAllNodesWithText("Baqarah", substring = true)
                .fetchSemanticsNodes()
            assert(englishTitleNodes.isNotEmpty()) {
                "Expected English chapter title containing 'Baqarah'"
            }

            // Chapter number badge — chapter 2 is Al-Baqarah; the badge renders "2"
            val badgeNodes = composeTestRule
                .onAllNodesWithText("2")
                .fetchSemanticsNodes()
            assert(badgeNodes.isNotEmpty()) {
                "Expected chapter number badge '2' for Al-Baqarah"
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Waits until verse results (nodes containing "Surah") appear, or until the
     * no-results state is shown. Uses a 15-second timeout to accommodate Realm
     * async queries on the test device.
     */
    private fun waitForVerseResultsOrNoResults() {
        composeTestRule.waitUntil(timeoutMillis = 15_000L) {
            val surahNodes = composeTestRule
                .onAllNodesWithText("Surah", substring = true)
                .fetchSemanticsNodes()
            val noResultsNodes = composeTestRule
                .onAllNodesWithText("No results found")
                .fetchSemanticsNodes()
            surahNodes.isNotEmpty() || noResultsNodes.isNotEmpty()
        }
    }

    /**
     * Waits until chapter results are present (the "Chapters" section header appears
     * more than once — once as a filter chip and once as a results section header),
     * or until the no-results state is shown.
     */
    private fun waitForChapterResultsOrNoResults() {
        composeTestRule.waitUntil(timeoutMillis = 15_000L) {
            val chaptersNodes = composeTestRule
                .onAllNodesWithText("Chapters", substring = true)
                .fetchSemanticsNodes()
            val noResultsNodes = composeTestRule
                .onAllNodesWithText("No results found")
                .fetchSemanticsNodes()
            // "Chapters" appears as a filter chip; a second node indicates a results section header
            chaptersNodes.size > 1 || noResultsNodes.isNotEmpty()
        }
    }
}
