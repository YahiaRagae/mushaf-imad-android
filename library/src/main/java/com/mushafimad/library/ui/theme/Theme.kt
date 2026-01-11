package com.mushafimad.library.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * MushafTheme - Main theme composable for the Mushaf library
 * Combines reading themes, color schemes, and typography
 *
 * Usage:
 * ```
 * MushafTheme(
 *     readingTheme = ReadingTheme.COMFORTABLE,
 *     colorScheme = ColorSchemeType.DEFAULT
 * ) {
 *     // Your composable content
 * }
 * ```
 */
@Composable
fun MushafTheme(
    readingTheme: ReadingTheme = ReadingTheme.COMFORTABLE,
    colorScheme: ColorSchemeType = ColorSchemeType.DEFAULT,
    content: @Composable () -> Unit
) {
    val materialColorScheme = when (colorScheme) {
        ColorSchemeType.DEFAULT -> createMaterialColorScheme(
            ColorSchemes.Default,
            readingTheme.isDark
        )
        ColorSchemeType.WARM -> createMaterialColorScheme(
            ColorSchemes.Warm,
            readingTheme.isDark
        )
        ColorSchemeType.COOL -> createMaterialColorScheme(
            ColorSchemes.Cool,
            readingTheme.isDark
        )
        ColorSchemeType.SEPIA -> createMaterialColorScheme(
            ColorSchemes.Sepia,
            readingTheme.isDark
        )
    }

    // Provide custom reading theme colors
    CompositionLocalProvider(
        LocalReadingTheme provides readingTheme,
        LocalMushafColors provides if (readingTheme.isDark) {
            MushafColors.darkColors()
        } else {
            MushafColors.lightColors()
        }
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = MushafMaterialTypography,
            content = content
        )
    }
}

/**
 * Color scheme type selector
 * Public API - exposed to library consumers
 */
enum class ColorSchemeType {
    DEFAULT,
    WARM,
    COOL,
    SEPIA
}

/**
 * Create Material3 ColorScheme from our custom color scheme
 */
private fun createMaterialColorScheme(
    scheme: Any,
    isDark: Boolean
): ColorScheme {
    // Use reflection to get color values from scheme object
    val schemeClass = scheme::class.java

    return ColorScheme(
        primary = getColorField(schemeClass, scheme, "primary"),
        onPrimary = getColorField(schemeClass, scheme, "onPrimary"),
        primaryContainer = getColorField(schemeClass, scheme, "primaryContainer"),
        onPrimaryContainer = getColorField(schemeClass, scheme, "onPrimaryContainer"),

        secondary = getColorField(schemeClass, scheme, "secondary"),
        onSecondary = getColorField(schemeClass, scheme, "onSecondary"),
        secondaryContainer = getColorField(schemeClass, scheme, "secondaryContainer"),
        onSecondaryContainer = getColorField(schemeClass, scheme, "onSecondaryContainer"),

        tertiary = getColorField(schemeClass, scheme, "tertiary"),
        onTertiary = getColorField(schemeClass, scheme, "onTertiary"),
        tertiaryContainer = getColorField(schemeClass, scheme, "tertiaryContainer"),
        onTertiaryContainer = getColorField(schemeClass, scheme, "onTertiaryContainer"),

        error = getColorField(schemeClass, scheme, "error"),
        onError = getColorField(schemeClass, scheme, "onError"),
        errorContainer = getColorField(schemeClass, scheme, "errorContainer"),
        onErrorContainer = getColorField(schemeClass, scheme, "onErrorContainer"),

        background = getColorField(schemeClass, scheme, "background"),
        onBackground = getColorField(schemeClass, scheme, "onBackground"),
        surface = getColorField(schemeClass, scheme, "surface"),
        onSurface = getColorField(schemeClass, scheme, "onSurface"),

        surfaceVariant = getColorField(schemeClass, scheme, "surfaceVariant"),
        onSurfaceVariant = getColorField(schemeClass, scheme, "onSurfaceVariant"),
        surfaceTint = getColorField(schemeClass, scheme, "primary"), // Use primary as surface tint
        outline = getColorField(schemeClass, scheme, "outline"),
        outlineVariant = getColorField(schemeClass, scheme, "outline"), // Reuse outline

        scrim = Color.Black.copy(alpha = 0.32f),
        inverseSurface = if (isDark) Color(0xFFE0E0E0) else Color(0xFF2C2C2C),
        inverseOnSurface = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0),
        inversePrimary = getColorField(schemeClass, scheme, "primary"),

        surfaceDim = getColorField(schemeClass, scheme, "surface"),
        surfaceBright = getColorField(schemeClass, scheme, "surface"),
        surfaceContainerLowest = getColorField(schemeClass, scheme, "surface"),
        surfaceContainerLow = getColorField(schemeClass, scheme, "surface"),
        surfaceContainer = getColorField(schemeClass, scheme, "surfaceVariant"),
        surfaceContainerHigh = getColorField(schemeClass, scheme, "surfaceVariant"),
        surfaceContainerHighest = getColorField(schemeClass, scheme, "surfaceVariant")
    )
}

/**
 * Get color field from scheme object using reflection
 */
private fun getColorField(clazz: Class<*>, instance: Any, fieldName: String): Color {
    return try {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.get(instance) as Color
    } catch (e: Exception) {
        Color.Magenta // Fallback color to make missing fields obvious
    }
}

/**
 * Composition local for current reading theme
 */
val LocalReadingTheme = staticCompositionLocalOf { ReadingTheme.COMFORTABLE }

/**
 * Composition local for Mushaf-specific colors
 */
val LocalMushafColors = staticCompositionLocalOf { MushafColors.lightColors() }

/**
 * Extension of MushafColors to provide theme-aware color sets
 */
private fun MushafColors.lightColors(): MushafColorSet {
    return MushafColorSet(
        verseHighlight = MushafColors.verseHighlightLight,
        selection = MushafColors.selectionLight,
        chapterHeaderBackground = MushafColors.chapterHeaderBackground,
        bismillahTint = MushafColors.bismillahTint,
        divider = MushafColors.divider,
        scrim = MushafColors.scrimLight
    )
}

private fun MushafColors.darkColors(): MushafColorSet {
    return MushafColorSet(
        verseHighlight = MushafColors.verseHighlightDark,
        selection = MushafColors.selectionDark,
        chapterHeaderBackground = MushafColors.chapterHeaderBackgroundDark,
        bismillahTint = MushafColors.bismillahTintDark,
        divider = MushafColors.dividerDark,
        scrim = MushafColors.scrimDark
    )
}

/**
 * Set of Mushaf-specific colors that adapt to theme
 */
data class MushafColorSet(
    val verseHighlight: Color,
    val selection: Color,
    val chapterHeaderBackground: Color,
    val bismillahTint: Color,
    val divider: Color,
    val scrim: Color
)

/**
 * Access current reading theme
 */
val MaterialTheme.readingTheme: ReadingTheme
    @Composable
    get() = LocalReadingTheme.current

/**
 * Access Mushaf-specific colors
 */
val MaterialTheme.mushafColors: MushafColorSet
    @Composable
    get() = LocalMushafColors.current
