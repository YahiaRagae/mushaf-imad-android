package com.mushafimad.ui.mushaf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.R
import com.mushafimad.ui.theme.MushafTypography
import com.mushafimad.ui.theme.mushafColors
import com.mushafimad.ui.theme.readingTheme

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
    mushafType: com.mushafimad.core.domain.models.MushafType = com.mushafimad.core.domain.models.MushafType.HAFS_1441,
    selectedVerse: Verse? = null,
    highlightedVerse: Verse? = null,
    onVerseClick: ((Verse) -> Unit)? = null,
    onVerseLongClick: ((Verse) -> Unit)? = null,
    onPageTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // One press-preview verse shared across all 15 line views on this page, so pressing
    // any fragment lights up every fragment of that verse. Resets when the page changes.
    var pressedVerse by remember { mutableStateOf<Verse?>(null) }
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
                juzNumber = juzNumber
            )

            // Lines container. A tap on the reading area that is NOT on a verse (verse
            // fragments consume their own taps) bubbles up to this Box and fires onPageTap
            // - the hook a host uses to toggle immersive/full-screen reading, matching iOS.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (onPageTap != null) {
                            Modifier.pointerInput(onPageTap) {
                                detectTapGestures { onPageTap() }
                            }
                        } else Modifier
                    )
            ) {
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
                        pressedVerse = pressedVerse,
                        onVerseClick = onVerseClick,
                        onVerseLongClick = onVerseLongClick,
                        onVersePressChange = { pressedVerse = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // The page-number ornament flows as the last item of the page - it is the
                // end of the page, not a pinned footer, so it sits right after the final
                // line (and only comes into view once the reader reaches the bottom),
                // exactly like the iOS viewer.
                item {
                    PageFooter(pageNumber = pageNumber)
                }
            }
            } // page-tap Box
        }
    }
}

/**
 * Page header showing page info
 */
@Composable
private fun PageHeader(
    chapters: List<Chapter>,
    juzNumber: Int,
    modifier: Modifier = Modifier
) {
    // Minimal header, matching the iOS viewer: juz on one side, the surah name on the other,
    // both in the same soft green with no heavy background bar. The surah name uses the same
    // calligraphic SurahName font iOS does (shipped in mushaf-core's assets). The page number
    // is no longer shown here - it appears as an ornament at the foot of the page.
    val accent = Color(0xFF5E8B6A)
    val context = LocalContext.current
    val surahNameFont = remember { FontFamily(Font("SurahName.otf", context.assets)) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "الجزء ${convertToArabicNumerals(juzNumber)}",
            style = MushafTypography.label,
            color = accent
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            chapters.forEach { chapter ->
                Text(
                    text = "سورة ${chapter.arabicTitle}",
                    fontFamily = surahNameFont,
                    fontSize = 26.sp,
                    color = accent
                )
            }
        }
    }
}

/**
 * Page-number ornament at the foot of the page, matching the iOS viewer: the dedicated
 * wide `pagenumb` frame (distinct from the verse fasel), with the number overlaid, sitting
 * on the left or right by page parity the way a facing-page Mushaf alternates it.
 */
@Composable
private fun PageFooter(pageNumber: Int, modifier: Modifier = Modifier) {
    // Which edge the ornament sits on. This mirrors iOS, which reads Page.isRight
    // from the shared quran.realm; that field is strict parity for all 604 pages
    // (odd = right, even = left), so computing it here is identical to the data
    // and saves plumbing the Page object down to the footer.
    val isRight = pageNumber % 2 == 1

    val ornament: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.pagenumb),
                contentDescription = null,
                modifier = Modifier.size(width = 54.dp, height = 36.dp)
            )
            Text(
                text = convertToArabicNumerals(pageNumber),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                color = Color(0xFF2B2B2B),
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }

    Row(
        // The reading lines already sit inside the list's 16dp horizontal inset, so a
        // further 14dp here lands the ornament ~30dp from the screen edge, as on iOS.
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRight) {
            ornament()
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
            ornament()
        }
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
