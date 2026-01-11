package com.mushafimad.library.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mushafimad.library.domain.models.Verse
import com.mushafimad.library.ui.theme.MushafTypography
import com.mushafimad.library.ui.theme.mushafColors
import com.mushafimad.library.ui.theme.readingTheme
import androidx.compose.ui.unit.LayoutDirection

/**
 * VerseComponent - Renders a single Quranic verse
 *
 * Displays verse text in Arabic with proper RTL layout, verse number,
 * and handles selection/highlighting
 *
 * @param verse The verse to display
 * @param isSelected Whether this verse is currently selected
 * @param isHighlighted Whether this verse should be highlighted
 * @param onClick Callback when verse is clicked
 * @param modifier Optional modifier
 */
@Composable
fun VerseComponent(
    verse: Verse,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val mushafColors = MaterialTheme.mushafColors
    val readingTheme = MaterialTheme.readingTheme

    // Determine background color based on state
    val backgroundColor = when {
        isSelected -> mushafColors.selection
        isHighlighted -> mushafColors.verseHighlight
        else -> Color.Transparent
    }

    // Force RTL layout for Arabic text
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            // Verse number (on the right in RTL)
            VerseNumber(
                verseNumber = verse.number,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Verse text (main content, fills remaining space)
            Text(
                text = verse.text,
                style = MushafTypography.verseText,
                color = readingTheme.textColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * VerseNumber - Renders the verse number in Arabic numerals
 */
@Composable
private fun VerseNumber(
    verseNumber: Int,
    modifier: Modifier = Modifier
) {
    val readingTheme = MaterialTheme.readingTheme

    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = MaterialTheme.mushafColors.divider.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = convertToArabicNumerals(verseNumber),
            style = MushafTypography.verseNumber,
            color = readingTheme.textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * VerseComponentLarge - Larger verse display for emphasis
 */
@Composable
fun VerseComponentLarge(
    verse: Verse,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val mushafColors = MaterialTheme.mushafColors
    val readingTheme = MaterialTheme.readingTheme

    val backgroundColor = if (isSelected) {
        mushafColors.selection
    } else {
        Color.Transparent
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Verse text with large style
            Text(
                text = verse.text,
                style = MushafTypography.verseTextLarge,
                color = readingTheme.textColor,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Verse number
            Text(
                text = "آية ${convertToArabicNumerals(verse.number)}",
                style = MushafTypography.label,
                color = readingTheme.textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * VerseComponentSmall - Compact verse display
 */
@Composable
fun VerseComponentSmall(
    verse: Verse,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val readingTheme = MaterialTheme.readingTheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verse number
            Text(
                text = convertToArabicNumerals(verse.number),
                style = MushafTypography.verseNumber.copy(
                    fontSize = MushafTypography.verseNumber.fontSize * 0.8f
                ),
                color = readingTheme.textColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 6.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Verse text
            Text(
                text = verse.text,
                style = MushafTypography.verseTextSmall,
                color = readingTheme.textColor,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
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
