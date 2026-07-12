package com.mushafimad.ui.search

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The search query lives in SearchViewModel, and SearchView must render it from
 * there.
 *
 * Previously SearchView kept its own `remember { mutableStateOf("") }` copy. A
 * host that swaps SearchView out when a result is tapped (which is the normal
 * pattern, and what the sample does) tears the composable down, so the local
 * copy reset to "" while the ViewModel kept the query and the results. Coming
 * back showed an empty search box sitting above stale results. Rotation hit the
 * same bug.
 */
@RunWith(AndroidJUnit4::class)
class SearchViewStateTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun queryAndResultsStayInSyncWhenTheHostNavigatesAwayAndBack() {
        var showSearch by mutableStateOf(true)

        compose.setContent {
            if (showSearch) {
                SearchView()
            } else {
                // Stand-in for the host navigating to the tapped result
                Text("reader")
            }
        }

        compose.onNode(hasSetTextAction()).performTextInput("Fatiha")
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTextSafe("Al-Fātiḥah")
        }

        // Host navigates away: SearchView leaves composition (its local state,
        // if any, is destroyed). The ViewModel survives.
        showSearch = false
        compose.waitForIdle()
        compose.onNodeWithText("reader").assertExists()

        // Host comes back
        showSearch = true
        compose.waitForIdle()

        // The field must still show the query - not an empty box above results
        compose.onNodeWithText("Fatiha").assertExists()
        compose.onNodeWithText("Al-Fātiḥah").assertExists()
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onAllNodesWithTextSafe(
        text: String
    ): Boolean = onAllNodes(
        androidx.compose.ui.test.hasText(text)
    ).fetchSemanticsNodes().isNotEmpty()
}
