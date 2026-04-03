package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.ui.player.ReciterPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterPickerDemoScreen(navController: NavHostController) {
    val audioRepository = remember { MushafLibrary.getAudioRepository() }
    var reciters by remember { mutableStateOf<List<ReciterInfo>>(emptyList()) }
    var selectedReciter by remember { mutableStateOf<ReciterInfo?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        reciters = audioRepository.getAllReciters()
        selectedReciter = audioRepository.getDefaultReciter()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReciterPickerDialog") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Selected Reciter:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Text(
                    text = selectedReciter?.nameArabic ?: "None",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = selectedReciter?.nameEnglish ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.Companion.height(32.dp))
                Button(onClick = { showPicker = true }) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.Companion.width(8.dp))
                    Text("Open ReciterPickerDialog")
                }
            }
        }

        if (showPicker && reciters.isNotEmpty()) {
            ReciterPickerDialog(
                reciters = reciters,
                selectedReciter = selectedReciter,
                onReciterSelected = {
                    selectedReciter = it
                    showPicker = false
                },
                onDismiss = { showPicker = false }
            )
        }
    }
}