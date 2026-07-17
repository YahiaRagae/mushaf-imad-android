package com.mushafimad.ui.mushaf

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.theme.MushafColors
import com.mushafimad.ui.theme.readingTheme
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranLineImageView(
    page: Int,
    line: Int,
    mushafType: MushafType,
    verses: List<Verse>,
    selectedVerse: Verse? = null,
    highlightedVerse: Verse? = null,
    pressedVerse: Verse? = null,
    onVerseClick: ((Verse) -> Unit)? = null,
    onVerseLongClick: ((Verse) -> Unit)? = null,
    onVersePressChange: ((Verse?) -> Unit)? = null,
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

            // Adjust line number: UI uses 1-15, data uses 0-14
            highlights.filter { it.line == (line - 1) }.forEach { highlight ->
                if (containerWidth > 0f && containerHeight > 0f) {
                    // Calculate highlight position (RTL-aware)
                    val visualLeftX = containerWidth * (1.0f - highlight.right)
                    val visualRightX = containerWidth * (1.0f - highlight.left)
                    val highlightWidth = visualRightX - visualLeftX
                    val highlightHeight = containerHeight * 0.94f

                    // The committed selection, the audio highlight, and the transient
                    // press preview all paint the same way. pressedVerse makes the WHOLE
                    // verse light up while a finger is down on any one of its fragments
                    // (matching iOS), instead of a per-fragment ripple on one line only.
                    val shouldHighlight =
                        verse == selectedVerse || verse == highlightedVerse || verse == pressedVerse

                    // Report press down/up for this fragment up to the page, which shares
                    // one pressedVerse across all lines so every fragment reacts together.
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    LaunchedEffect(isPressed) {
                        onVersePressChange?.invoke(if (isPressed) verse else null)
                    }
                    // If this fragment leaves composition while still pressed - e.g. the page
                    // scrolls out from under the finger - the effect above is cancelled without
                    // ever reporting the release, which would leave the preview stuck on a verse
                    // that scrolled away. Clear it on disposal to guarantee it never sticks.
                    DisposableEffect(Unit) {
                        onDispose { if (isPressed) onVersePressChange?.invoke(null) }
                    }

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
                            .combinedClickable(
                                interactionSource = interactionSource,
                                // No ripple: the whole-verse background flip IS the feedback.
                                indication = null,
                                enabled = onVerseClick != null || onVerseLongClick != null,
                                onClick = { onVerseClick?.invoke(verse) },
                                onLongClick = onVerseLongClick?.let { cb -> { cb(verse) } }
                            )
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

            // Markers use 0-14 indexing like highlights, adjust for UI's 1-15
            if (marker != null && marker.line == (line - 1) && containerWidth > 0f && containerHeight > 0f) {
                // Transform percentage coordinates to screen pixels (RTL-aware for X)
                val markerX = containerWidth * (1.0f - marker.centerX)
                val markerY = containerHeight * marker.centerY

                // Marker WIDTH = 5.4% of the line width - the width of the blank slot the
                // Mushaf reserves for the verse number. The height follows the ornament's
                // taller-than-wide aspect (see VerseFasel), so the number stays readable
                // without the marker spilling sideways onto the text. Mirrors the iOS renderer.
                val markerWidth = 0.054f * containerWidth
                val markerHeight = markerWidth / (92f / 117f)

                val adjustedX = markerX - (markerWidth / 2f)
                val adjustedY = markerY - (markerHeight / 2f)

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { adjustedX.toDp() },
                            y = with(density) { adjustedY.toDp() }
                        )
                ) {
                    VerseFasel(
                        number = verse.number,
                        sizeInPx = markerWidth
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
