package com.mushafimad.library.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mushafimad.library.R

/**
 * Quran-specific fonts loaded from assets
 * Matching iOS FontRegistrar fonts
 */
object QuranFonts {
    /**
     * Main Quran text font - UthmanicHafs (KFGQPC)
     * Used for verse text in Hafs layout
     */
    val UthmanicHafs = FontFamily(
        Font(R.font.uthmanic_hafs1_ver17, FontWeight.Normal)
    )

    /**
     * Alternative Quran text font - UthmanTaha (Naskh)
     * Used for traditional layout
     */
    val UthmanTaha = FontFamily(
        Font(R.font.uthman_tn1_ver20, FontWeight.Normal),
        Font(R.font.uthman_tn1b_ver20, FontWeight.Bold)
    )

    /**
     * Quran chapter titles font
     * Used for chapter headers and names
     */
    val QuranTitles = FontFamily(
        Font(R.font.quran_titles, FontWeight.Normal)
    )

    /**
     * Quran numbers font
     * Used for verse numbers and page markers
     */
    val QuranNumbers = FontFamily(
        Font(R.font.quran_numbers, FontWeight.Normal)
    )

    /**
     * Al-Quran Al-Kareem font
     * Alternative verse font
     */
    val AlQuranAlKareem = FontFamily(
        Font(R.font.al_quran_al_kareem_regular, FontWeight.Normal)
    )

    /**
     * HafsSmart font
     * Smart Hafs font with advanced features
     */
    val HafsSmart = FontFamily(
        Font(R.font.hafs_smart_08, FontWeight.Normal)
    )

    /**
     * Kitab font for UI text
     * Used for translations and UI elements
     */
    val Kitab = FontFamily(
        Font(R.font.kitab_regular, FontWeight.Normal),
        Font(R.font.kitab_bold, FontWeight.Bold)
    )
}

/**
 * Mushaf typography system
 * Defines text styles for different use cases
 */
object MushafTypography {
    /**
     * Verse text style - main Quran text
     */
    val verseText = TextStyle(
        fontFamily = QuranFonts.UthmanicHafs,
        fontSize = 24.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    )

    /**
     * Large verse text for emphasis
     */
    val verseTextLarge = TextStyle(
        fontFamily = QuranFonts.UthmanicHafs,
        fontSize = 32.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    )

    /**
     * Small verse text for compact display
     */
    val verseTextSmall = TextStyle(
        fontFamily = QuranFonts.UthmanicHafs,
        fontSize = 18.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    /**
     * Verse number style
     */
    val verseNumber = TextStyle(
        fontFamily = QuranFonts.QuranNumbers,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    /**
     * Chapter title style (Arabic)
     */
    val chapterTitle = TextStyle(
        fontFamily = QuranFonts.QuranTitles,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    /**
     * Chapter name style (English/Translation)
     */
    val chapterName = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    )

    /**
     * Page number style
     */
    val pageNumber = TextStyle(
        fontFamily = QuranFonts.QuranNumbers,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    /**
     * Juz/Part marker style
     */
    val juzMarker = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /**
     * Bismillah style (بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ)
     */
    val bismillah = TextStyle(
        fontFamily = QuranFonts.UthmanicHafs,
        fontSize = 26.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )

    /**
     * Translation text style
     */
    val translation = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    /**
     * UI label style
     */
    val label = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    /**
     * UI body text style
     */
    val body = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
}

/**
 * Material3 Typography configuration for the library
 * Uses Kitab font for standard Material components
 */
val MushafMaterialTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = QuranFonts.Kitab,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
