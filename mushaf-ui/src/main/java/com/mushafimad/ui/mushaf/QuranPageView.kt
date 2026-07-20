package com.mushafimad.ui.mushaf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.mushafimad.core.domain.models.ChapterHeader
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.R
import com.mushafimad.ui.theme.MushafTypography
import com.mushafimad.ui.theme.QuranFonts
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
    juzLabel: String,
    hizbLabel: String? = null,
    chapterHeaders: List<ChapterHeader> = emptyList(),
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(readingTheme.backgroundColor)
        ) {
            val pageVerses = verses.filter { it.pageNumber == pageNumber }
            // A tap on the reading area that is NOT on a verse (verse fragments consume
            // their own taps) fires onPageTap - the hook a host uses to toggle
            // immersive/full-screen reading, matching iOS.
            val tapModifier = if (onPageTap != null) {
                Modifier.pointerInput(onPageTap) {
                    detectTapGestures { onPageTap() }
                }
            } else Modifier
            // The 15 lines, rendered at a given pitch - the line is the unit of measure,
            // as in a printed Mushaf and the iOS viewer. Each line frame is shorter than
            // the image's natural height; QuranLineImageView crops the top/bottom
            // whitespace to fit. Lines run the FULL page width - iOS pads only the header
            // row, not the line stack, and everything on the line (text scale, surah-name
            // bar) derives from this width.
            val lineStack: @Composable (androidx.compose.ui.unit.Dp) -> Unit = { lineHeight ->
                // Render 15 lines (1-15) as images - skip line 0 which is often empty
                repeat(15) { index ->
                    val line = index + 1  // Start from line 1 instead of 0
                    QuranLineImageView(
                        page = pageNumber,
                        line = line,
                        mushafType = mushafType,
                        verses = pageVerses,
                        chapterHeaders = chapterHeaders,
                        selectedVerse = selectedVerse,
                        highlightedVerse = highlightedVerse,
                        pressedVerse = pressedVerse,
                        onVerseClick = onVerseClick,
                        onVerseLongClick = onVerseLongClick,
                        onVersePressChange = { pressedVerse = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineHeight)
                    )
                }
            }

            if (maxWidth > maxHeight) {
                // LANDSCAPE: the width-derived line pitch is far taller than the screen,
                // so - exactly like iOS (QuranPageView.swift:70-97) - the page becomes a
                // vertical scroll: header, 15 lines at the width-derived pitch (x0.7,
                // iOS's landscape crop factor), and the footer inside the scroll content,
                // padded 40dp. The footer ornament scales 2.5x like iOS's
                // deviceScaleFactor so it doesn't look lost on the wide page.
                val lineHeight = maxWidth / (1440f / 232f) * 0.7f
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .then(tapModifier)
                ) {
                    PageHeader(
                        chapters = chapters,
                        juzLabel = juzLabel,
                        hizbLabel = hizbLabel
                    )
                    lineStack(lineHeight)
                    PageFooter(
                        pageNumber = pageNumber,
                        scale = 2.5f,
                        modifier = Modifier.padding(vertical = 40.dp)
                    )
                }
            } else {
                // PORTRAIT: fit to the screen, no scrolling; header and footer fixed.
                Column(modifier = Modifier.fillMaxSize()) {
                    PageHeader(
                        chapters = chapters,
                        juzLabel = juzLabel,
                        hizbLabel = hizbLabel
                    )
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .then(tapModifier)
                    ) {
                        // Line pitch. iOS pins every line to a width-derived height (the
                        // 1440x232 image aspect x a 0.73 crop factor,
                        // QuranPageView.swift:68,118) and lets one trailing spacer take
                        // the leftover - which on iPhone aspect ratios is ~ZERO, so iOS
                        // pages visually FILL the space down to the footer. Android
                        // phones are taller: the same pinned pitch left ~12% of the
                        // content area as a blank band at the bottom (#114). So:
                        //  - FULL pages (content on all 15 lines - every page but 1 and
                        //    2) take the equal share of the available height, filling to
                        //    the footer the way iOS looks on its own hardware; capped at
                        //    the image's natural height so an extreme aspect can never
                        //    letterbox a line.
                        //  - SPARSE pages keep the iOS pinned pitch, so their blank slots
                        //    stay small instead of stretching the few real lines apart
                        //    (#110). Pages 1 and 2 are this mushaf's only sparse pages:
                        //    they ship 7 of their 15 line PNGs as blank placeholders
                        //    (bismillah lines carry no verse/header DATA, so this cannot
                        //    be derived from the page data; it is a property of the
                        //    shipped assets).
                        val isSparse = pageNumber <= 2
                        val scaledImageHeight = maxWidth / (1440f / 232f)
                        val lineHeight = if (isSparse) {
                            minOf(scaledImageHeight * 0.73f, maxHeight / 15)
                        } else {
                            minOf(scaledImageHeight, maxHeight / 15)
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            lineStack(lineHeight)
                        }
                    }

                    // Page-number ornament, anchored at the bottom of the page. The page
                    // fits without scrolling, so it is always visible, matching iOS.
                    PageFooter(pageNumber = pageNumber)
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
    juzLabel: String,
    hizbLabel: String? = null,
    modifier: Modifier = Modifier
) {
    // Minimal header, matching the iOS viewer: juz (and the hizb, where one starts on this
    // page) on one side, the surah name on the other, both in the reading theme's accent
    // green (iOS's brand900, which flips to its pale variant on Night) with no heavy
    // background bar. The juz/hizb labels are the localized titles carried in the page
    // data (Arabic or English, chosen for the app language); the surah name uses the same
    // calligraphic SurahName font iOS does (shipped in mushaf-core's assets). The page number
    // is no longer shown here - it appears as an ornament at the foot of the page.
    val accent = MaterialTheme.readingTheme.accentColor
    val context = LocalContext.current
    val surahNameFont = remember { FontFamily(Font("SurahName.otf", context.assets)) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = juzLabel,
                style = MushafTypography.label,
                color = accent
            )
            hizbLabel?.let { hizb ->
                Text(
                    text = hizb,
                    style = MushafTypography.label,
                    color = accent
                )
            }
        }

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
private fun PageFooter(pageNumber: Int, scale: Float = 1f, modifier: Modifier = Modifier) {
    // Which edge the ornament sits on. This mirrors iOS, which reads Page.isRight
    // from the shared quran.realm; that field is strict parity for all 604 pages
    // (odd = right, even = left), so computing it here is identical to the data
    // and saves plumbing the Page object down to the footer.
    val isRight = pageNumber % 2 == 1

    val ornament: @Composable () -> Unit = {
        // iOS PageFooterView frames pagenumb at 42x26 in portrait and overlays the number
        // at font size 32 in KFGQPCUthmanTahaNaskh-Bold, letting minimumScaleFactor shrink
        // it to fit. What actually binds is the HEIGHT: this font's line box is ~1.68em, so
        // a 32pt line is ~54pt tall in a 26pt frame and SwiftUI scales it to an effective
        // ~15pt for every page number (verified by measuring a simulator screenshot's ink).
        // Compose has no auto-shrink, so reproduce it: measure the number once at 32sp and
        // scale by whatever factor fits BOTH the frame's width and height. The text is
        // drawn unbounded so the line box is never clipped (iOS overlays don't clip either).
        Box(
            contentAlignment = Alignment.Center,
            // scale mirrors iOS's deviceScaleFactor: 1.0 in portrait, 2.5 in the
            // landscape scroll layout, applied to the frame and the digit alike.
            modifier = Modifier.size(width = 42.dp * scale, height = 26.dp * scale)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pagenumb),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            val text = convertToArabicNumerals(pageNumber)
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val numberStyle = TextStyle(
                fontFamily = QuranFonts.UthmanTaha,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp * scale
            )
            val fontSize = remember(text, density, scale) {
                val budgetW = with(density) { (42.dp * scale).toPx() }
                val budgetH = with(density) { (26.dp * scale).toPx() }
                val size = measurer.measure(text, numberStyle, maxLines = 1, softWrap = false).size
                val factor = minOf(1f, budgetW / size.width, budgetH / size.height)
                32.sp * scale * factor
            }
            Text(
                text = text,
                style = numberStyle,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                // Deliberately NOT theme-derived: the pagenumb ornament's interior is
                // opaque white in every theme (same SVG on iOS), so black is the only
                // readable digit colour. iOS's .primary would go white-on-white on Night.
                color = Color.Black,
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .offset(y = (-2).dp)
            )
        }
    }

    Row(
        // ~30dp inset from the screen edge, matching iOS's PageFooterView hPadding.
        // Minimal vertical padding so the ornament hugs the bottom of the page.
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 2.dp),
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
