package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.sampleapp.component.PreferenceItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesDataScreen(navController: NavHostController) {
    val preferencesRepository = remember { MushafLibrary.getPreferencesRepository() }
    val coroutineScope = rememberCoroutineScope()

    val currentPage by preferencesRepository.getCurrentPageFlow().collectAsState(initial = 1)
    val mushafType by preferencesRepository.getMushafTypeFlow().collectAsState(initial = MushafType.HAFS_1441)
    val playbackSpeed by preferencesRepository.getPlaybackSpeedFlow().collectAsState(initial = 1.0f)
    val repeatMode by preferencesRepository.getRepeatModeFlow().collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PreferencesRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.Companion.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Current Preferences (Live)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.Companion.padding(bottom = 16.dp)
                )
            }

            item {
                PreferenceItem(
                    title = "getCurrentPageFlow()",
                    value = "Page $currentPage",
                    onIncrement = {
                        coroutineScope.launch {
                            preferencesRepository.setCurrentPage(currentPage + 1)
                        }
                    },
                    onDecrement = {
                        coroutineScope.launch {
                            if (currentPage > 1) preferencesRepository.setCurrentPage(currentPage - 1)
                        }
                    }
                )
            }

            item {
                PreferenceItem(
                    title = "getMushafTypeFlow()",
                    value = mushafType.name,
                    onIncrement = null,
                    onDecrement = null
                )
            }

            item {
                PreferenceItem(
                    title = "getPlaybackSpeedFlow()",
                    value = "${playbackSpeed}x",
                    onIncrement = {
                        coroutineScope.launch {
                            preferencesRepository.setPlaybackSpeed(
                                (playbackSpeed + 0.25f).coerceAtMost(
                                    3.0f
                                )
                            )
                        }
                    },
                    onDecrement = {
                        coroutineScope.launch {
                            preferencesRepository.setPlaybackSpeed(
                                (playbackSpeed - 0.25f).coerceAtLeast(
                                    0.5f
                                )
                            )
                        }
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "getRepeatModeFlow()",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (repeatMode) "Enabled" else "Disabled",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Switch(
                            checked = repeatMode,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    preferencesRepository.setRepeatMode(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}