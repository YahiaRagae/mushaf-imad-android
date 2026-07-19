package com.mushafimad.ui.theme

import androidx.compose.ui.text.font.FontFamily
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * #97: the digit font must be a real shipped font, not a silent fall-back to
 * FontFamily.Default — that regression is invisible to compilation and to every
 * non-visual test, so it is pinned here.
 */
class QuranFontsTest {

    @Test
    fun uthmanTahaIsARealFontNotTheSystemDefault() {
        assertThat(QuranFonts.UthmanTaha).isNotEqualTo(FontFamily.Default)
    }
}
