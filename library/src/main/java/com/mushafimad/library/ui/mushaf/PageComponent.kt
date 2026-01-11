package com.mushafimad.library.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * PageComponent - Renders a complete Quran page
 *
 * Displays page header, bismillah (when needed), and verses
 * Handles chapter boundaries and proper RTL layout
 *
 * @param verses List of verses on this page
 * @param chapters Chapters that appear on this page
 * @param pageNumber Current page number
 * @param juzNumber Juz number for this page
 * @param selectedVerse Currently selected verse (if any)
 * @param highlightedVerses Set of verses to highlight
 * @param onVerseClick Callback when a verse is clicked
 * @param scrollToPosition Scroll position (0-1) to restore
 * @param onScrollPositionChange Callback when scroll position changes
 * @param modifier Optional modifier
 */
@Composable
fun PageComponent(
    verses: List<Verse>,
    chapters: List<Chapter>,
    pageNumber: Int,
    juzNumber: Int,
    selectedVerse: Verse? = null,
    highlightedVerses: Set<Verse> = emptySet(),
    onVerseClick: ((Verse) -> Unit)? = null,
    scrollToPosition: Float = 0f,
    onScrollPositionChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val readingTheme = MaterialTheme.readingTheme

    // Restore scroll position
    LaunchedEffect(scrollToPosition) {
        if (scrollToPosition > 0f && verses.isNotEmpty()) {
            val targetIndex = (verses.size * scrollToPosition).toInt()
                .coerceIn(0, verses.size - 1)
            listState.scrollToItem(targetIndex)
        }
    }

    // Report scroll position changes
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (verses.isNotEmpty()) {
            val position = listState.firstVisibleItemIndex.toFloat() / verses.size
            onScrollPositionChange?.invoke(position)
        }
    }

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

            // Verses list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Group verses by chapter for bismillah insertion
                val groupedVerses = verses.groupBy { it.chapterNumber }

                groupedVerses.forEach { (chapterNumber, chapterVerses) ->
                    // Check if this is the start of a new chapter
                    val isChapterStart = chapterVerses.firstOrNull()?.number == 1

                    // Show chapter header if chapter starts on this page
                    if (isChapterStart) {
                        val chapter = chapters.find { it.number == chapterNumber }
                        if (chapter != null) {
                            item(key = "chapter_header_$chapterNumber") {
                                ChapterHeader(chapter = chapter)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // Show bismillah (except for chapter 1 and 9)
                        if (chapterNumber != 1 && chapterNumber != 9) {
                            item(key = "bismillah_$chapterNumber") {
                                Bismillah()
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Render verses
                    items(
                        items = chapterVerses,
                        key = { verse -> "verse_${verse.chapterNumber}_${verse.number}" }
                    ) { verse ->
                        VerseComponent(
                            verse = verse,
                            isSelected = verse == selectedVerse,
                            isHighlighted = verse in highlightedVerses,
                            onClick = onVerseClick?.let { { it(verse) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * PageHeader - Shows page info at the top
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
 * ChapterHeader - Shows chapter name when a new chapter starts
 */
@Composable
private fun ChapterHeader(
    chapter: Chapter,
    modifier: Modifier = Modifier
) {
    val mushafColors = MaterialTheme.mushafColors
    val readingTheme = MaterialTheme.readingTheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = mushafColors.chapterHeaderBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arabic name
        Text(
            text = chapter.arabicTitle,
            style = MushafTypography.chapterTitle,
            color = readingTheme.textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // English name and info
        Text(
            text = "${chapter.englishTitle} • ${chapter.versesCount} آيات",
            style = MushafTypography.label,
            color = readingTheme.textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Bismillah - "In the name of Allah, the Most Gracious, the Most Merciful"
 */
@Composable
private fun Bismillah(
    modifier: Modifier = Modifier
) {
    val mushafColors = MaterialTheme.mushafColors
    val readingTheme = MaterialTheme.readingTheme

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = MushafTypography.bismillah,
            color = mushafColors.bismillahTint,
            textAlign = TextAlign.Center
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
