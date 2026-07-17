package com.mushafimad.ui.mushaf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mushafimad.ui.R
import com.mushafimad.ui.theme.QuranFonts
import com.mushafimad.ui.theme.readingTheme

/**
 * VerseFasel - Decorative verse number marker
 *
 * Renders verse number in Arabic-Indic numerals with decorative ornamental background
 */
@Composable
fun VerseFasel(
    number: Int,
    scale: Float = 1.0f,
    sizeInPx: Float? = null,  // Optional size in pixels, overrides scale-based calculation
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // The fasel ornament is taller than it is wide (native 92x117). iOS renders it at
    // that aspect so the marker fits the narrow slot the Mushaf reserves for the number
    // (its width) while staying tall enough to read (its height). Rendering it square, as
    // before, forced a bad choice: wide enough to read meant spilling onto the adjacent
    // letter; narrow enough not to spill meant it was too small.
    val ornamentWtoH = 92f / 117f

    // sizeInPx, when provided, is the target WIDTH in px; the height follows the aspect.
    val widthDp = sizeInPx?.let { px -> with(density) { px.toDp() } } ?: (21 * 1.2f * scale).dp
    val heightDp = widthDp / ornamentWtoH
    val fontSizeSp = with(density) { (heightDp.toPx() * 0.46f).toSp() }

    Box(
        modifier = modifier
            .width(widthDp)
            .height(heightDp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative ornamental background, stretched to the ornament's own aspect
        Image(
            painter = painterResource(id = R.drawable.fasel),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = convertToArabicNumerals(number),
            fontSize = fontSizeSp,
            fontFamily = QuranFonts.UthmanTaha,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
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

// Preview Functions

@Preview(name = "VerseFasel - Light Theme", showBackground = true)
@Composable
private fun VerseFaselPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier
                .background(Color(0xFFE4EFD9))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerseFasel(number = 1, scale = 1.0f)
            VerseFasel(number = 22, scale = 1.0f)
            VerseFasel(number = 286, scale = 1.0f)
        }
    }
}

@Preview(name = "VerseFasel - Dark Theme", showBackground = true, backgroundColor = 0xFF2F352F)
@Composable
private fun VerseFaselDarkPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier
                .background(Color(0xFF2F352F))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerseFasel(number = 100, scale = 1.0f)
            VerseFasel(number = 200, scale = 1.0f)
            VerseFasel(number = 286, scale = 1.0f)
        }
    }
}

@Preview(name = "VerseFasel - Different Scales", showBackground = true)
@Composable
private fun VerseFaselScalesPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier
                .background(Color(0xFFE4EFD9))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerseFasel(number = 23, scale = 1.0f)
            VerseFasel(number = 123, scale = 2.0f)
            VerseFasel(number = 123, scale = 3.0f)
        }
    }
}
