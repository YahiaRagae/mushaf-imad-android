package com.mushafimad.sampleapp.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme

@Composable
fun ThemeSettingsSheet(
    selectedReadingTheme: ReadingTheme,
    selectedColorScheme: ColorSchemeType,
    onReadingThemeChange: (ReadingTheme) -> Unit,
    onColorSchemeChange: (ColorSchemeType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Theme Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.Companion.padding(bottom = 16.dp)
        )

        // Reading Theme Selection
        Text(
            text = "Reading Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.Companion.padding(bottom = 8.dp)
        )

        ReadingTheme.entries.forEach { theme ->
            FilterChip(
                selected = theme == selectedReadingTheme,
                onClick = { onReadingThemeChange(theme) },
                label = { Text(theme.name) },
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.Companion.height(16.dp))

        // Color Scheme Selection
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.Companion.padding(bottom = 8.dp)
        )

        ColorSchemeType.entries.forEach { scheme ->
            FilterChip(
                selected = scheme == selectedColorScheme,
                onClick = { onColorSchemeChange(scheme) },
                label = { Text(scheme.name) },
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.Companion.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.Companion.height(32.dp))
    }
}