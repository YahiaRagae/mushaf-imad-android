package com.mushafimad.ui.mushaf

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * #105: the Mushaf is an Arabic book, so swiping right must always turn to the
 * next page — even when the host app is LTR. Both tests wrap MushafView in an
 * explicit LTR layout direction to simulate an English-locale host.
 */
@RunWith(AndroidJUnit4::class)
class PageTurnDirectionTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun swipeRightTurnsToNextPageInLtrHost() {
        var shown: Int? = null
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MushafView(initialPage = 50, onPageChanged = { shown = it })
            }
        }
        compose.waitUntil(timeoutMillis = 20_000) { shown == 50 }
        compose.waitForIdle()
        Thread.sleep(1500)

        compose.onRoot().performTouchInput { swipeRight() }
        compose.waitUntil(timeoutMillis = 5_000) { shown != 50 }

        assertThat(shown).isEqualTo(51) // forward through the Mushaf
    }

    @Test
    fun swipeLeftTurnsToPreviousPageInLtrHost() {
        var shown: Int? = null
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MushafView(initialPage = 50, onPageChanged = { shown = it })
            }
        }
        compose.waitUntil(timeoutMillis = 20_000) { shown == 50 }
        compose.waitForIdle()
        Thread.sleep(1500)

        compose.onRoot().performTouchInput { swipeLeft() }
        compose.waitUntil(timeoutMillis = 5_000) { shown != 50 }

        assertThat(shown).isEqualTo(49) // backward through the Mushaf
    }
}
