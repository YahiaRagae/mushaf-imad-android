package com.mushafimad.library.ui.mushaf

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mushafimad.library.R
import com.mushafimad.library.ui.theme.QuranFonts
import com.mushafimad.library.ui.theme.readingTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight

/**
 * VerseFasel - Decorative verse number marker
 * Matches iOS VerseFasel.swift implementation
 *
 * Renders verse number in Arabic numerals with decorative background
 */
@Composable
fun VerseFasel(
    number: Int,
    scale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val readingTheme = MaterialTheme.readingTheme

    // iOS dimensions (balance = 3.69) - using moderate scale for visibility
    val balance = 1.2f  // Balanced size - visible but not blocking
    val baseWidth = 21 * balance      // ~25dp
    val baseHeight = 27 * balance     // ~32dp
    val baseFontSize = 14 * balance   // ~17sp

    Box(
        modifier = modifier
            .size(
                width = (baseWidth * scale).dp,
                height = (baseHeight * scale).dp
            )
            .offset(x = (-2).dp, y = (-4).dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative fasel background (matching iOS)
        Image(
            painter = painterResource(id = R.drawable.fasel),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(readingTheme.textColor)
        )

        // Arabic numeral overlay
        Text(
            text = convertToArabicNumerals(number),
            fontSize = (baseFontSize * scale * 0.8f).sp,
            fontFamily = QuranFonts.UthmanTaha,
            fontWeight = FontWeight.Bold,
            color = readingTheme.backgroundColor,  // Inverted for better visibility
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = (2 * scale).dp)
                .offset(x = (-1 * scale).dp, y = (1 * scale).dp)
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
