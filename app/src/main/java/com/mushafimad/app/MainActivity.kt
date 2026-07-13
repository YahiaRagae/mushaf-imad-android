package com.mushafimad.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import com.mushafimad.app.nav.QuranApp
import com.mushafimad.app.ui.AppSettings
import com.mushafimad.ui.theme.MushafTheme

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Media notifications for the library's background playback service.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val readingTheme by AppSettings.readingTheme.collectAsState()
            val colorScheme by AppSettings.colorScheme.collectAsState()

            // MushafTheme is part of the library's public API.
            MushafTheme(readingTheme = readingTheme, colorScheme = colorScheme) {
                QuranApp()
            }
        }
    }
}
