package com.mushafimad.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.ui.mushaf.MushafView
import com.mushafimad.library.ui.theme.ColorSchemeType
import com.mushafimad.library.ui.theme.ReadingTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sample app demonstrating MushafView usage
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SampleAppContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleAppContent() {
    var showSettings by remember { mutableStateOf(false) }
    var selectedReadingTheme by remember { mutableStateOf(ReadingTheme.COMFORTABLE) }
    var selectedColorScheme by remember { mutableStateOf(ColorSchemeType.DEFAULT) }
    var selectedMushafType by remember { mutableStateOf(MushafType.HAFS_1441) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mushaf Imad - Week 10 Demo") },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafView(
                readingTheme = selectedReadingTheme,
                colorScheme = selectedColorScheme,
                mushafType = selectedMushafType,
                showNavigationControls = true,
                showPageInfo = true,
                onVerseSelected = { verse ->
                    // Handle verse selection
                    println("Verse selected: ${verse.chapterNumber}:${verse.number}")
                },
                onPageChanged = { page ->
                    // Handle page changes
                    println("Page changed to: $page")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Settings bottom sheet
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false }
        ) {
            SettingsSheet(
                selectedReadingTheme = selectedReadingTheme,
                selectedColorScheme = selectedColorScheme,
                selectedMushafType = selectedMushafType,
                onReadingThemeChange = { selectedReadingTheme = it },
                onColorSchemeChange = { selectedColorScheme = it },
                onMushafTypeChange = { selectedMushafType = it },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
fun SettingsSheet(
    selectedReadingTheme: ReadingTheme,
    selectedColorScheme: ColorSchemeType,
    selectedMushafType: MushafType,
    onReadingThemeChange: (ReadingTheme) -> Unit,
    onColorSchemeChange: (ColorSchemeType) -> Unit,
    onMushafTypeChange: (MushafType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Reading Theme Selection
        Text(
            text = "Reading Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ReadingTheme.entries.forEach { theme ->
            FilterChip(
                selected = theme == selectedReadingTheme,
                onClick = { onReadingThemeChange(theme) },
                label = { Text(theme.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Scheme Selection
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ColorSchemeType.entries.forEach { scheme ->
            FilterChip(
                selected = scheme == selectedColorScheme,
                onClick = { onColorSchemeChange(scheme) },
                label = { Text(scheme.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mushaf Type Selection
        Text(
            text = "Mushaf Type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MushafType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedMushafType,
                onClick = { onMushafTypeChange(type) },
                label = { Text(type.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
