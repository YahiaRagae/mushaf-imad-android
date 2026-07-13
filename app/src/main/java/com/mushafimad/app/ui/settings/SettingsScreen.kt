package com.mushafimad.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.app.ui.AppSettings
import com.mushafimad.app.ui.toCore
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.ui.player.ReciterPickerDialog
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val preferencesRepository = MushafLibrary.getPreferencesRepository()
    private val audioRepository = MushafLibrary.getAudioRepository()
    private val dataExportRepository = MushafLibrary.getDataExportRepository()

    val mushafType: StateFlow<MushafType> = preferencesRepository.getMushafTypeFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MushafType.HAFS_1441)

    val selectedReciter: StateFlow<ReciterInfo?> = audioRepository.getSelectedReciterFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _reciters = MutableStateFlow<List<ReciterInfo>>(emptyList())
    val reciters: StateFlow<List<ReciterInfo>> = _reciters.asStateFlow()

    private val _exportSize = MutableStateFlow<Int?>(null)
    val exportSize: StateFlow<Int?> = _exportSize.asStateFlow()

    init {
        viewModelScope.launch { _reciters.value = audioRepository.getAllReciters() }
    }

    fun setMushafType(type: MushafType) = viewModelScope.launch {
        preferencesRepository.setMushafType(type)
    }

    fun setColorScheme(scheme: ColorSchemeType) = viewModelScope.launch {
        preferencesRepository.setColorScheme(scheme.toCore())
    }

    fun selectReciter(reciter: ReciterInfo) = viewModelScope.launch {
        audioRepository.saveSelectedReciter(reciter)
        preferencesRepository.setSelectedReciterId(reciter.id)
    }

    /** Proves DataExportRepository round-trips user data. */
    fun exportUserData() = viewModelScope.launch {
        _exportSize.value = dataExportRepository.exportToJson(includeHistory = true).length
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val readingTheme by AppSettings.readingTheme.collectAsStateWithLifecycle()
    val colorScheme by AppSettings.colorScheme.collectAsStateWithLifecycle()
    val mushafType by viewModel.mushafType.collectAsStateWithLifecycle()
    val reciters by viewModel.reciters.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val exportSize by viewModel.exportSize.collectAsStateWithLifecycle()

    var showReciterPicker by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Section("Reading theme (library ReadingTheme)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReadingTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = theme == readingTheme,
                        onClick = { AppSettings.setReadingTheme(theme) },
                        label = { Text(theme.name) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Section("Color scheme (persisted by PreferencesRepository)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSchemeType.entries.forEach { scheme ->
                    FilterChip(
                        selected = scheme == colorScheme,
                        onClick = { viewModel.setColorScheme(scheme) },
                        label = { Text(scheme.name) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Section("Mushaf layout (persisted by PreferencesRepository)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MushafType.entries.forEach { type ->
                    FilterChip(
                        selected = type == mushafType,
                        onClick = { viewModel.setMushafType(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Section("Reciter (AudioRepository - ${reciters.size} available)")
            Button(
                onClick = { showReciterPicker = true },
                enabled = reciters.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedReciter?.nameEnglish ?: "Choose a reciter")
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Section("Data export (DataExportRepository)")
            Button(onClick = { viewModel.exportUserData() }, modifier = Modifier.fillMaxWidth()) {
                Text("Export user data to JSON")
            }
            exportSize?.let {
                Text("Exported $it chars of JSON", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showReciterPicker) {
        // ReciterPickerDialog is public API of mushaf-ui.
        ReciterPickerDialog(
            reciters = reciters,
            selectedReciter = selectedReciter,
            onReciterSelected = {
                viewModel.selectReciter(it)
                showReciterPicker = false
            },
            onDismiss = { showReciterPicker = false }
        )
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}
