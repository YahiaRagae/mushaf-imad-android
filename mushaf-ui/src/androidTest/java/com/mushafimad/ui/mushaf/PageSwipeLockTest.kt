package com.mushafimad.ui.mushaf

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * #88: pageSwipeEnabled lets a host lock page-turning by swipe. A swipe-lock is invisible
 * in a screenshot, so it is verified here: with it off a swipe must NOT change the page,
 * with it on (the default) a swipe must.
 */
@RunWith(AndroidJUnit4::class)
class PageSwipeLockTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun swipeDoesNotTurnPageWhenDisabled() {
        var shown: Int? = null
        compose.setContent {
            MushafView(initialPage = 50, pageSwipeEnabled = false, onPageChanged = { shown = it })
        }
        compose.waitUntil(timeoutMillis = 20_000) { shown == 50 }
        compose.waitForIdle()
        Thread.sleep(1500)

        compose.onRoot().performTouchInput { swipeLeft() }
        compose.waitForIdle()
        Thread.sleep(1000)

        assertThat(shown).isEqualTo(50) // locked: page did not change
    }

    @Test
    fun swipeTurnsPageWhenEnabled() {
        var shown: Int? = null
        compose.setContent {
            MushafView(initialPage = 50, pageSwipeEnabled = true, onPageChanged = { shown = it })
        }
        compose.waitUntil(timeoutMillis = 20_000) { shown == 50 }
        compose.waitForIdle()
        Thread.sleep(1500)

        compose.onRoot().performTouchInput { swipeLeft() }
        compose.waitUntil(timeoutMillis = 5_000) { shown != 50 }

        assertThat(shown).isNotEqualTo(50) // default: swipe turned the page
    }
}
