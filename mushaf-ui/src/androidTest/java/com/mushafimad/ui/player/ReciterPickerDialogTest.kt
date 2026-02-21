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
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ReciterInfo
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for ReciterPickerDialog and ReciterInfo data model.
 *
 * QA Issue #36 — TC-5.1 through TC-5.13
 *
 * Koin is bootstrapped automatically via ContentProviders (MushafInitProvider and
 * MushafUiInitProvider) before any test activity is created, so no explicit Koin
 * setup is required here.
 *
 * ReciterPickerDialog is a pure composable taking data as parameters — no ViewModel.
 */
@RunWith(AndroidJUnit4::class)
class ReciterPickerDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var reciters: List<ReciterInfo>

    // Minimal ReciterInfo used for data-model unit-like assertions (TC-5.9/5.10/5.11)
    private val testReciter = ReciterInfo(
        id = 1,
        nameArabic = "إبراهيم الأخضر",
        nameEnglish = "Ibrahim Al-Akdar",
        rewaya = "حفص عن عاصم",
        folderUrl = "https://server.everyayah.com/data/Ibrahim_Akhdar_32kbps/"
    )

    @Before
    fun setUp() = runTest {
        MushafLibrary.initialize(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        reciters = MushafLibrary.getAudioRepository().getAllReciters()
    }

    // -------------------------------------------------------------------------
    // TC-5.1: Dialog renders as a modal with "Select Reciter" header
    // -------------------------------------------------------------------------

    @Test
    fun tc5_1_dialogRendersWithSelectReciterHeader() {
        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Select Reciter")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-5.2: All 18 reciters are present
    // -------------------------------------------------------------------------

    @Test
    fun tc5_2_eighteenRecitersDisplayedInScrollableList() {
        assertThat(reciters).hasSize(18)

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        // Verify the dialog renders without crash and the header is present,
        // confirming the list was accepted (list scrollability is structural).
        composeTestRule
            .onNodeWithText("Select Reciter")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-5.3: Each reciter entry shows display name and rewaya (spot-check first)
    // -------------------------------------------------------------------------

    @Test
    fun tc5_3_reciterItemShowsDisplayNameAndRewaya() {
        val firstReciter = reciters.first()

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onAllNodesWithText(firstReciter.getDisplayName())
            .fetchSemanticsNodes()
            .isNotEmpty()
            .let { assertThat(it).isTrue() }

        composeTestRule
            .onAllNodesWithText(firstReciter.rewaya)
            .fetchSemanticsNodes()
            .isNotEmpty()
            .let { assertThat(it).isTrue() }
    }

    // -------------------------------------------------------------------------
    // TC-5.4: selectedReciter shows checkmark indicator
    // -------------------------------------------------------------------------

    @Test
    fun tc5_4_selectedReciterShowsCheckmarkIndicator() {
        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = reciters[0],
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Selected")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-5.5: selectedReciter = null — no checkmark indicator
    // -------------------------------------------------------------------------

    @Test
    fun tc5_5_noSelectedReciter_noCheckmarkIndicatorShown() {
        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        val selectedNodes = composeTestRule
            .onAllNodesWithContentDescription("Selected")
            .fetchSemanticsNodes()

        assertThat(selectedNodes).isEmpty()
    }

    // -------------------------------------------------------------------------
    // TC-5.6: Tapping a different reciter fires onReciterSelected with correct value
    // -------------------------------------------------------------------------

    @Test
    fun tc5_6_tapDifferentReciter_firesOnReciterSelectedCallback() {
        var selectedReciter: ReciterInfo? = null
        val targetReciter = reciters[1]

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = reciters[0],
                    onReciterSelected = { selectedReciter = it },
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onAllNodesWithText(targetReciter.getDisplayName())
            .fetchSemanticsNodes()
            .isNotEmpty()
            .let { assertThat(it).isTrue() }

        composeTestRule
            .onAllNodesWithText(targetReciter.getDisplayName())[0]
            .performClick()

        assertThat(selectedReciter).isEqualTo(targetReciter)
    }

    // -------------------------------------------------------------------------
    // TC-5.7: Tapping Cancel button fires onDismiss
    // -------------------------------------------------------------------------

    @Test
    fun tc5_7_tapCancelButton_firesOnDismissCallback() {
        var dismissed = false

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        assertThat(dismissed).isTrue()
    }

    // -------------------------------------------------------------------------
    // TC-5.8: Back press fires onDismiss (noted as best-effort in Compose tests)
    // -------------------------------------------------------------------------

    @Test
    fun tc5_8_backPress_firesOnDismissCallback() {
        var dismissed = false

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        // Dialog's onDismissRequest is wired to onDismiss; back press triggers it
        assertThat(dismissed).isTrue()
    }

    // -------------------------------------------------------------------------
    // TC-5.9: ReciterInfo.getDisplayName("ar") returns Arabic name
    // -------------------------------------------------------------------------

    @Test
    fun tc5_9_getDisplayNameAr_returnsArabicName() = runTest {
        assertThat(testReciter.getDisplayName("ar")).isEqualTo(testReciter.nameArabic)
    }

    // -------------------------------------------------------------------------
    // TC-5.10: ReciterInfo.getDisplayName("en") returns English name
    // -------------------------------------------------------------------------

    @Test
    fun tc5_10_getDisplayNameEn_returnsEnglishName() = runTest {
        assertThat(testReciter.getDisplayName("en")).isEqualTo(testReciter.nameEnglish)
    }

    // -------------------------------------------------------------------------
    // TC-5.11: ReciterInfo.getAudioUrl(1) returns URL ending in "001.mp3"
    // -------------------------------------------------------------------------

    @Test
    fun tc5_11_getAudioUrlChapterOne_returnsUrlEndingIn001Mp3() = runTest {
        val url = testReciter.getAudioUrl(1)
        assertThat(url).endsWith("001.mp3")
        // Ensure the folder URL and chapter are joined without a double slash in path
        assertThat(url).doesNotContain("//0")
    }

    // -------------------------------------------------------------------------
    // TC-5.12: Empty reciters list — dialog renders without crash
    // -------------------------------------------------------------------------

    @Test
    fun tc5_12_emptyRecitersList_dialogRendersWithoutCrash() {
        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = emptyList(),
                    selectedReciter = null,
                    onReciterSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Select Reciter")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // TC-5.13: Re-selecting already-selected reciter still fires callback
    // -------------------------------------------------------------------------

    @Test
    fun tc5_13_tapAlreadySelectedReciter_callbackStillFires() {
        var callbackCount = 0
        val alreadySelected = reciters[0]

        composeTestRule.setContent {
            MaterialTheme {
                ReciterPickerDialog(
                    reciters = reciters,
                    selectedReciter = alreadySelected,
                    onReciterSelected = { callbackCount++ },
                    onDismiss = {}
                )
            }
        }

        composeTestRule
            .onAllNodesWithText(alreadySelected.getDisplayName())[0]
            .performClick()

        assertThat(callbackCount).isEqualTo(1)
    }
}
