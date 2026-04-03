package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mushafimad.sampleapp.component.ReciterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersDataScreen(navController: NavHostController) {
    val audioRepository = remember { MushafLibrary.getAudioRepository() }
    var reciters by remember { mutableStateOf<List<ReciterInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        reciters = audioRepository.getAllReciters()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AudioRepository - Reciters") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.Companion.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Companion.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "getAllReciters() - ${reciters.size} reciters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.Companion.padding(bottom = 8.dp)
                    )
                }
                itemsIndexed(reciters) { index, reciter ->
                    ReciterItem(index + 1, reciter)
                }
            }
        }
    }
}