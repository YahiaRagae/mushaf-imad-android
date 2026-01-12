package com.mushafimad.library.ui.mushaf

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Verse
import com.mushafimad.library.ui.theme.MushafColors
import com.mushafimad.library.ui.theme.readingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Displays a single Quran line image loaded from assets
 * Matches iOS QuranLineImageView implementation
 *
 * Images are stored in assets/quran-images/{page}/{line}.png
 * Original dimensions: 1440 x 232 pixels
 * Each page has 15 lines (0-14)
 */
@Composable
fun QuranLineImageView(
    page: Int,
    line: Int,
    mushafType: MushafType,
    verses: List<Verse>,
    selectedVerse: Verse? = null,
    highlightedVerse: Verse? = null,
    onVerseClick: ((Verse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val readingTheme = MaterialTheme.readingTheme
    val density = LocalDensity.current

    var imageBitmap by remember(page, line) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var containerWidth by remember { mutableStateOf(0f) }
    var containerHeight by remember { mutableStateOf(0f) }

    // Original image dimensions (iOS: originalLineSize)
    val originalWidth = 1440f
    val originalHeight = 232f
    val imageAspect = originalWidth / originalHeight

    // Load image from assets
    LaunchedEffect(page, line) {
        imageBitmap = loadLineImage(context, page, line)
    }

    // Calculate scale based on container width
    val lineScale = if (containerWidth > 0f) containerWidth / originalWidth else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(imageAspect)
            .onGloballyPositioned { coordinates ->
                with(density) {
                    containerWidth = coordinates.size.width.toFloat()
                    containerHeight = coordinates.size.height.toFloat()
                }
            }
    ) {
        // Render line image
        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Quran page $page line $line",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(readingTheme.textColor),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Render verse highlights
        verses.forEach { verse ->
            val highlights = when (mushafType) {
                MushafType.HAFS_1441 -> verse.highlights1441
                MushafType.HAFS_1405 -> verse.highlights1405
            }

            highlights.filter { it.line == line }.forEach { highlight ->
                if (containerWidth > 0f && containerHeight > 0f) {
                    // Calculate highlight position (RTL-aware)
                    val visualLeftX = containerWidth * (1.0f - highlight.right)
                    val visualRightX = containerWidth * (1.0f - highlight.left)
                    val highlightWidth = visualRightX - visualLeftX
                    val highlightHeight = containerHeight * 0.94f

                    // Determine if this verse should be highlighted
                    val shouldHighlight = verse == selectedVerse || verse == highlightedVerse

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { visualLeftX.toDp() },
                                y = with(density) { (containerHeight * 0.03f).toDp() }
                            )
                            .size(
                                width = with(density) { highlightWidth.toDp() },
                                height = with(density) { highlightHeight.toDp() }
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (shouldHighlight) {
                                    if (readingTheme.isDark) {
                                        MushafColors.selectionDark
                                    } else {
                                        MushafColors.selectionLight
                                    }
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                            .clickable(enabled = onVerseClick != null) {
                                onVerseClick?.invoke(verse)
                            }
                    )
                }
            }
        }

        // Render verse numbers
        verses.forEach { verse ->
            val marker = when (mushafType) {
                MushafType.HAFS_1441 -> verse.marker1441
                MushafType.HAFS_1405 -> verse.marker1405
            }

            if (marker != null && marker.line == line && containerWidth > 0f && containerHeight > 0f) {
                // Calculate marker position (RTL-aware)
                val markerX = containerWidth * (1.0f - marker.centerX)
                val markerY = containerHeight * marker.centerY

                // Adjust offset to center the marker better
                val adjustedX = markerX - (21f * 1.2f * lineScale / 2f)  // Center horizontally
                val adjustedY = markerY - (27f * 1.2f * lineScale / 2f)  // Center vertically

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { adjustedX.toDp() },
                            y = with(density) { adjustedY.toDp() }
                        )
                ) {
                    VerseFasel(
                        number = verse.number,
                        scale = lineScale
                    )
                }
            }
        }
    }
}

/**
 * Load line image from assets
 * Path: assets/quran-images/{page}/{line}.png
 */
private suspend fun loadLineImage(
    context: Context,
    page: Int,
    line: Int
): androidx.compose.ui.graphics.ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        val assetPath = "quran-images/$page/$line.png"
        val inputStream: InputStream = context.assets.open(assetPath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        println("QuranLineImageView: Failed to load image for page $page line $line: ${e.message}")
        null
    }
}
