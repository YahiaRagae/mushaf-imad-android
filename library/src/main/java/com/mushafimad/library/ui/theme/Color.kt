package com.mushafimad.library.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Reading theme colors matching iOS ReadingTheme enum
 * Public API - exposed to library consumers
 */
enum class ReadingTheme(val backgroundColor: Color, val textColor: Color) {
    /**
     * Comfortable green theme - default
     */
    COMFORTABLE(
        backgroundColor = Color(0xFFE4EFD9),
        textColor = Color(0xFF000000)
    ),

    /**
     * Calm blue-green theme
     */
    CALM(
        backgroundColor = Color(0xFFE0F1EA),
        textColor = Color(0xFF000000)
    ),

    /**
     * Night dark theme
     */
    NIGHT(
        backgroundColor = Color(0xFF2F352F),
        textColor = Color(0xFFFFFFFF)
    ),

    /**
     * Pure white theme
     */
    WHITE(
        backgroundColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF000000)
    );

    /**
     * Check if theme is dark
     */
    val isDark: Boolean
        get() = this == NIGHT
}

/**
 * Color scheme colors from ThemeRepository
 */
object ColorSchemes {
    /**
     * Default color scheme
     */
    object Default {
        val primary = Color(0xFF6750A4)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFEADDFF)
        val onPrimaryContainer = Color(0xFF21005D)

        val secondary = Color(0xFF625B71)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFE8DEF8)
        val onSecondaryContainer = Color(0xFF1D192B)

        val tertiary = Color(0xFF7D5260)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFFFD8E4)
        val onTertiaryContainer = Color(0xFF31111D)

        val error = Color(0xFFB3261E)
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFF9DEDC)
        val onErrorContainer = Color(0xFF410E0B)

        val background = Color(0xFFFFFBFE)
        val onBackground = Color(0xFF1C1B1F)
        val surface = Color(0xFFFFFBFE)
        val onSurface = Color(0xFF1C1B1F)

        val outline = Color(0xFF79747E)
        val surfaceVariant = Color(0xFFE7E0EC)
        val onSurfaceVariant = Color(0xFF49454F)
    }

    /**
     * Warm color scheme - warmer tones
     */
    object Warm {
        val primary = Color(0xFFB4714F)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFFFDBCA)
        val onPrimaryContainer = Color(0xFF3A0F00)

        val secondary = Color(0xFF77574E)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFFFDBCA)
        val onSecondaryContainer = Color(0xFF2C160F)

        val tertiary = Color(0xFF695E2F)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFF2E2A7)
        val onTertiaryContainer = Color(0xFF221B00)

        val error = Color(0xFFBA1A1A)
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFFFDAD6)
        val onErrorContainer = Color(0xFF410002)

        val background = Color(0xFFFFFBFF)
        val onBackground = Color(0xFF201A18)
        val surface = Color(0xFFFFFBFF)
        val onSurface = Color(0xFF201A18)

        val outline = Color(0xFF85736D)
        val surfaceVariant = Color(0xFFF5DED6)
        val onSurfaceVariant = Color(0xFF53433E)
    }

    /**
     * Cool color scheme - cooler blue tones
     */
    object Cool {
        val primary = Color(0xFF006C4C)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFF89F8C7)
        val onPrimaryContainer = Color(0xFF002114)

        val secondary = Color(0xFF4D6357)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFCFE9D9)
        val onSecondaryContainer = Color(0xFF092016)

        val tertiary = Color(0xFF3D6373)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFC1E8FB)
        val onTertiaryContainer = Color(0xFF001F29)

        val error = Color(0xFFBA1A1A)
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFFFDAD6)
        val onErrorContainer = Color(0xFF410002)

        val background = Color(0xFFFBFDF9)
        val onBackground = Color(0xFF191C1A)
        val surface = Color(0xFFFBFDF9)
        val onSurface = Color(0xFF191C1A)

        val outline = Color(0xFF6F7975)
        val surfaceVariant = Color(0xFFDBE5DF)
        val onSurfaceVariant = Color(0xFF404943)
    }

    /**
     * Sepia color scheme - warm vintage tones
     */
    object Sepia {
        val primary = Color(0xFF8B6914)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFFFDF9F)
        val onPrimaryContainer = Color(0xFF2C1F00)

        val secondary = Color(0xFF6A5D3F)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFF3E1BB)
        val onSecondaryContainer = Color(0xFF241A04)

        val tertiary = Color(0xFF4A6547)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFCCEBC5)
        val onTertiaryContainer = Color(0xFF072109)

        val error = Color(0xFFBA1A1A)
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFFFDAD6)
        val onErrorContainer = Color(0xFF410002)

        val background = Color(0xFFFFFBFF)
        val onBackground = Color(0xFF1E1B16)
        val surface = Color(0xFFFFFBFF)
        val onSurface = Color(0xFF1E1B16)

        val outline = Color(0xFF7B7667)
        val surfaceVariant = Color(0xFFECE1CF)
        val onSurfaceVariant = Color(0xFF4C4639)
    }
}

/**
 * Additional UI colors
 */
object MushafColors {
    // Verse highlight colors
    val verseHighlightLight = Color(0xFFFFEB3B).copy(alpha = 0.3f)
    val verseHighlightDark = Color(0xFFFDD835).copy(alpha = 0.4f)

    // Selection colors
    val selectionLight = Color(0xFF2196F3).copy(alpha = 0.2f)
    val selectionDark = Color(0xFF64B5F6).copy(alpha = 0.3f)

    // Chapter header colors
    val chapterHeaderBackground = Color(0xFFF5F5F5)
    val chapterHeaderBackgroundDark = Color(0xFF424242)

    // Bismillah colors
    val bismillahTint = Color(0xFF4CAF50)
    val bismillahTintDark = Color(0xFF81C784)

    // Divider colors
    val divider = Color(0xFFE0E0E0)
    val dividerDark = Color(0xFF616161)

    // Overlay colors
    val scrimLight = Color(0xFF000000).copy(alpha = 0.32f)
    val scrimDark = Color(0xFF000000).copy(alpha = 0.64f)
}
