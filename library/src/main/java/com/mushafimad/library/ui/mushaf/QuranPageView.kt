package com.mushafimad.library.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mushafimad.library.domain.models.Chapter
import com.mushafimad.library.domain.models.Verse
import com.mushafimad.library.ui.theme.MushafTypography
import com.mushafimad.library.ui.theme.mushafColors
import com.mushafimad.library.ui.theme.readingTheme

/**
 * Quran page view using line images (matching iOS implementation)
 *
 * Each page consists of 15 lines rendered as images
 * Images are loaded from assets/quran-images/{page}/{line}.png
 *
 * @param verses List of verses on this page (used for metadata and click handling)
 * @param chapters Chapters that appear on this page
 * @param pageNumber Current page number (1-604)
 * @param juzNumber Juz number for this page
 * @param selectedVerse Currently selected verse
 * @param highlightedVerse Verse to highlight
 * @param onVerseClick Callback when a line/verse is clicked
 * @param modifier Optional modifier
 */
@Composable
fun QuranPageView(
    verses: List<Verse>,
    chapters: List<Chapter>,
    pageNumber: Int,
    juzNumber: Int,
    mushafType: com.mushafimad.library.domain.models.MushafType = com.mushafimad.library.domain.models.MushafType.HAFS_1441,
    selectedVerse: Verse? = null,
    highlightedVerse: Verse? = null,
    onVerseClick: ((Verse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val readingTheme = MaterialTheme.readingTheme
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Calculate dimensions matching iOS (aspect ratio 1440:232 per line)
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val lineHeight = screenWidth / 1440f * 232f

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(readingTheme.backgroundColor)
        ) {
            // Page header
            PageHeader(
                chapters = chapters,
                pageNumber = pageNumber,
                juzNumber = juzNumber
            )

            HorizontalDivider(
                color = MaterialTheme.mushafColors.divider,
                thickness = 1.dp
            )

            // Lines container
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Render 15 lines (1-15) as images - skip line 0 which is often empty
                items(15) { index ->
                    val line = index + 1  // Start from line 1 instead of 0
                    QuranLineImageView(
                        page = pageNumber,
                        line = line,
                        mushafType = mushafType,
                        verses = verses.filter { it.pageNumber == pageNumber },
                        selectedVerse = selectedVerse,
                        highlightedVerse = highlightedVerse,
                        onVerseClick = onVerseClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Page header showing page info
 */
@Composable
private fun PageHeader(
    chapters: List<Chapter>,
    pageNumber: Int,
    juzNumber: Int,
    modifier: Modifier = Modifier
) {
    val mushafColors = MaterialTheme.mushafColors
    val readingTheme = MaterialTheme.readingTheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(mushafColors.chapterHeaderBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Page number
        Text(
            text = "صفحة ${convertToArabicNumerals(pageNumber)}",
            style = MushafTypography.label,
            color = readingTheme.textColor.copy(alpha = 0.8f)
        )

        // Center: Chapter name(s)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            chapters.forEach { chapter ->
                Text(
                    text = chapter.arabicTitle,
                    style = MushafTypography.chapterTitle.copy(
                        fontSize = MushafTypography.chapterTitle.fontSize * 0.8f
                    ),
                    color = readingTheme.textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Right: Juz number
        Text(
            text = "جزء ${convertToArabicNumerals(juzNumber)}",
            style = MushafTypography.label,
            color = readingTheme.textColor.copy(alpha = 0.8f)
        )
    }
}

/**
 * Convert Western numerals to Arabic-Indic numerals
 */
private fun convertToArabicNumerals(number: Int): String {
    val arabicNumerals = mapOf(
        '0' to '٠',
        '1' to '١',
        '2' to '٢',
        '3' to '٣',
        '4' to '٤',
        '5' to '٥',
        '6' to '٦',
        '7' to '٧',
        '8' to '٨',
        '9' to '٩'
    )

    return number.toString().map { arabicNumerals[it] ?: it }.joinToString("")
}
