package com.mushafimad.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Bookmark
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.mushaf.MushafWithPlayerView
import com.mushafimad.ui.player.ReciterPickerDialog
import com.mushafimad.ui.search.SearchView
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme
import kotlinx.coroutines.launch

/**
 * Sample app demonstrating MushafView and Audio Player integration
 * Structured like iOS example app with categories
 */
class MainActivity : ComponentActivity() {

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - background audio notifications will work
        } else {
            // Permission denied - background audio will still work but without notification controls
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+ (required for playback controls)
        requestNotificationPermissionIfNeeded()

        setContent {
            MaterialTheme {
                SampleAppContent()
            }
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission for Android 13+ (API 33+)
     * This is required to show playback controls notification
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

// Navigation routes
sealed class Screen(val route: String) {
    object Home : Screen("home")

    // Core Library demos
    object ChaptersData : Screen("core_chapters")
    object VersesData : Screen("core_verses")
    object RecitersData : Screen("core_reciters")
    object BookmarksData : Screen("core_bookmarks")
    object ReadingHistoryData : Screen("core_history")
    object PreferencesData : Screen("core_preferences")

    // UI Library demos
    object MushafReader : Screen("ui_mushaf")
    object MushafWithAudio : Screen("ui_mushaf_audio")
    object SearchDemo : Screen("ui_search")
    object ThemeCustomization : Screen("ui_theme")
    object ReciterPickerDemo : Screen("ui_reciter_picker")
}

@Composable
fun SampleAppContent() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Core Library demos
        composable(Screen.ChaptersData.route) {
            ChaptersDataScreen(navController = navController)
        }
        composable(Screen.VersesData.route) {
            VersesDataScreen(navController = navController)
        }
        composable(Screen.RecitersData.route) {
            RecitersDataScreen(navController = navController)
        }
        composable(Screen.BookmarksData.route) {
            BookmarksDataScreen(navController = navController)
        }
        composable(Screen.ReadingHistoryData.route) {
            ReadingHistoryDataScreen(navController = navController)
        }
        composable(Screen.PreferencesData.route) {
            PreferencesDataScreen(navController = navController)
        }

        // UI Library demos
        composable(Screen.MushafReader.route) {
            MushafReaderScreen(navController = navController)
        }
        composable(Screen.MushafWithAudio.route) {
            MushafWithAudioScreen(navController = navController)
        }
        composable(Screen.SearchDemo.route) {
            SearchDemoScreen(navController = navController)
        }
        composable(Screen.ThemeCustomization.route) {
            ThemeCustomizationScreen(navController = navController)
        }
        composable(Screen.ReciterPickerDemo.route) {
            ReciterPickerDemoScreen(navController = navController)
        }
    }
}

// ============================================================================
// MARK: - Home Screen (Two Sections: Core & UI)
// ============================================================================

data class DemoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val coreItems = listOf(
        DemoItem(
            title = "Chapters",
            subtitle = "ChapterRepository - All 114 surahs",
            icon = Icons.Default.List,
            route = Screen.ChaptersData.route
        ),
        DemoItem(
            title = "Verses",
            subtitle = "VerseRepository - Ayat data & search",
            icon = Icons.Default.Menu,
            route = Screen.VersesData.route
        ),
        DemoItem(
            title = "Reciters",
            subtitle = "AudioRepository - 18 available reciters",
            icon = Icons.Default.Person,
            route = Screen.RecitersData.route
        ),
        DemoItem(
            title = "Bookmarks",
            subtitle = "BookmarkRepository - Save & manage",
            icon = Icons.Default.Favorite,
            route = Screen.BookmarksData.route
        ),
        DemoItem(
            title = "Reading History",
            subtitle = "ReadingHistoryRepository - Stats & tracking",
            icon = Icons.Default.DateRange,
            route = Screen.ReadingHistoryData.route
        ),
        DemoItem(
            title = "Preferences",
            subtitle = "PreferencesRepository - User settings",
            icon = Icons.Default.Settings,
            route = Screen.PreferencesData.route
        )
    )

    val uiItems = listOf(
        DemoItem(
            title = "MushafView",
            subtitle = "Basic Quran page reader",
            icon = Icons.Default.Home,
            route = Screen.MushafReader.route
        ),
        DemoItem(
            title = "MushafWithPlayerView",
            subtitle = "Mushaf with integrated audio player",
            icon = Icons.Default.PlayArrow,
            route = Screen.MushafWithAudio.route
        ),
        DemoItem(
            title = "SearchView",
            subtitle = "Search verses and chapters",
            icon = Icons.Default.Search,
            route = Screen.SearchDemo.route
        ),
        DemoItem(
            title = "Theme System",
            subtitle = "ReadingTheme & ColorScheme customization",
            icon = Icons.Default.Build,
            route = Screen.ThemeCustomization.route
        ),
        DemoItem(
            title = "ReciterPickerDialog",
            subtitle = "Reciter selection dialog",
            icon = Icons.Default.AccountCircle,
            route = Screen.ReciterPickerDemo.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MushafImad Library") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Core Library section
            item {
                SectionHeader(
                    title = "Core Library",
                    subtitle = "mushaf-core: Data layer & repositories"
                )
            }
            items(coreItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // UI Library section
            item {
                SectionHeader(
                    title = "UI Library",
                    subtitle = "mushaf-ui: Jetpack Compose components"
                )
            }
            items(uiItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DemoListItem(
    item: DemoItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.title) },
        supportingContent = { Text(item.subtitle) },
        leadingContent = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// ============================================================================
// MARK: - Core Library Demo Screens
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersDataScreen(navController: NavHostController) {
    val chapterRepository = remember { MushafLibrary.getChapterRepository() }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        chapters = chapterRepository.getAllChapters()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChapterRepository") },
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "getAllChapters() - ${chapters.size} chapters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(chapters) { chapter ->
                    ChapterItem(chapter)
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(chapter: Chapter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${chapter.number}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.arabicTitle,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${chapter.englishTitle} - ${chapter.versesCount} verses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (chapter.isMeccan) "Meccan" else "Medinan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersesDataScreen(navController: NavHostController) {
    val verseRepository = remember { MushafLibrary.getVerseRepository() }
    var verses by remember { mutableStateOf<List<Verse>>(emptyList()) }
    var selectedPage by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedPage) {
        isLoading = true
        verses = verseRepository.getVersesForPage(selectedPage)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VerseRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Page selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (selectedPage > 1) selectedPage-- },
                    enabled = selectedPage > 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                }
                Text(
                    text = "Page $selectedPage / 604",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { if (selectedPage < 604) selectedPage++ },
                    enabled = selectedPage < 604
                ) {
                    Icon(Icons.Default.ArrowForward, "Next")
                }
            }

            HorizontalDivider()

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "getVersesForPage($selectedPage) - ${verses.size} verses",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(verses) { verse ->
                        VerseItem(verse)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseItem(verse: Verse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${verse.chapterNumber}:${verse.number}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Page ${verse.pageNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = verse.text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "getAllReciters() - ${reciters.size} reciters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                itemsIndexed(reciters) { index, reciter ->
                    ReciterItem(index + 1, reciter)
                }
            }
        }
    }
}

@Composable
private fun ReciterItem(index: Int, reciter: ReciterInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reciter.nameArabic,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = reciter.nameEnglish,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ID: ${reciter.id} | ${reciter.rewaya}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksDataScreen(navController: NavHostController) {
    val bookmarkRepository = remember { MushafLibrary.getBookmarkRepository() }
    var bookmarks by remember { mutableStateOf<List<Bookmark>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        bookmarks = bookmarkRepository.getAllBookmarks()
        isLoading = false
    }

    fun refreshBookmarks() {
        coroutineScope.launch {
            bookmarks = bookmarkRepository.getAllBookmarks()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BookmarkRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        // Add a sample bookmark
                        val randomChapter = (1..114).random()
                        val randomVerse = (1..7).random()
                        bookmarkRepository.addBookmark(
                            chapterNumber = randomChapter,
                            verseNumber = randomVerse,
                            pageNumber = 1,
                            note = "Sample bookmark"
                        )
                        refreshBookmarks()
                    }
                }
            ) {
                Icon(Icons.Default.Add, "Add Bookmark")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "getAllBookmarks() - ${bookmarks.size} bookmarks",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Tap + to add a random bookmark",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (bookmarks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "No bookmarks yet. Tap + to add one.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(bookmarks) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onDelete = {
                                coroutineScope.launch {
                                    bookmarkRepository.deleteBookmark(bookmark.id)
                                    refreshBookmarks()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItem(bookmark: Bookmark, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Surah ${bookmark.chapterNumber}, Ayah ${bookmark.verseNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (bookmark.note.isNotEmpty()) {
                    Text(
                        text = bookmark.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryDataScreen(navController: NavHostController) {
    val historyRepository = remember { MushafLibrary.getReadingHistoryRepository() }
    var totalReadingTime by remember { mutableStateOf(0L) }
    var currentStreak by remember { mutableStateOf(0) }
    var readChapters by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        totalReadingTime = historyRepository.getTotalReadingTime()
        currentStreak = historyRepository.getCurrentStreak()
        readChapters = historyRepository.getReadChapters()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReadingHistoryRepository") },
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "Reading Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    StatCard(
                        title = "getTotalReadingTime()",
                        value = formatDuration(totalReadingTime),
                        subtitle = "Total time spent reading"
                    )
                }

                item {
                    StatCard(
                        title = "getCurrentStreak()",
                        value = "$currentStreak days",
                        subtitle = "Consecutive days with reading activity"
                    )
                }

                item {
                    StatCard(
                        title = "getReadChapters()",
                        value = "${readChapters.size} / 114",
                        subtitle = "Chapters read: ${if (readChapters.isEmpty()) "None yet" else readChapters.take(10).joinToString(", ") + if (readChapters.size > 10) "..." else ""}"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m ${secs}s"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Current Preferences (Live)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
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
                            preferencesRepository.setPlaybackSpeed((playbackSpeed + 0.25f).coerceAtMost(3.0f))
                        }
                    },
                    onDecrement = {
                        coroutineScope.launch {
                            preferencesRepository.setPlaybackSpeed((playbackSpeed - 0.25f).coerceAtLeast(0.5f))
                        }
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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

@Composable
private fun PreferenceItem(
    title: String,
    value: String,
    onIncrement: (() -> Unit)?,
    onDecrement: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (onDecrement != null && onIncrement != null) {
                Row {
                    IconButton(onClick = onDecrement) {
                        Icon(Icons.Default.KeyboardArrowDown, "Decrease")
                    }
                    IconButton(onClick = onIncrement) {
                        Icon(Icons.Default.KeyboardArrowUp, "Increase")
                    }
                }
            }
        }
    }
}

// ============================================================================
// MARK: - UI Library Demo Screens
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafReaderScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MushafView") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafView(
                readingTheme = ReadingTheme.COMFORTABLE,
                colorScheme = ColorSchemeType.DEFAULT,
                mushafType = MushafType.HAFS_1441,
                // null = resume where the reader left off. Pass a page number
                // instead to always open on that page (see ThemeCustomization).
                initialPage = null,
                showNavigationControls = true,
                showPageInfo = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafWithAudioScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MushafWithPlayerView") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafWithPlayerView(
                readingTheme = ReadingTheme.COMFORTABLE,
                colorScheme = ColorSchemeType.DEFAULT,
                mushafType = MushafType.HAFS_1441,
                initialPage = 1,
                showNavigationControls = true,
                showPageInfo = true,
                showAudioPlayer = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDemoScreen(navController: NavHostController) {
    val verseRepository = remember { MushafLibrary.getVerseRepository() }
    var currentPage by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (currentPage != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Page $currentPage") },
                    navigationIcon = {
                        IconButton(onClick = { currentPage = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to search")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                MushafView(
                    readingTheme = ReadingTheme.COMFORTABLE,
                    colorScheme = ColorSchemeType.DEFAULT,
                    mushafType = MushafType.HAFS_1441,
                    initialPage = currentPage,
                    showNavigationControls = true,
                    showPageInfo = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SearchView") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                SearchView(
                    onVerseSelected = { verse ->
                        currentPage = verse.pageNumber
                    },
                    onChapterSelected = { chapter ->
                        coroutineScope.launch {
                            val firstVerse = verseRepository.getVerse(chapter.number, 1)
                            currentPage = firstVerse?.pageNumber ?: 1
                        }
                    },
                    onDismiss = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationScreen(navController: NavHostController) {
    var selectedTheme by remember { mutableStateOf(ReadingTheme.COMFORTABLE) }
    var selectedColorScheme by remember { mutableStateOf(ColorSchemeType.DEFAULT) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme System") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafView(
                readingTheme = selectedTheme,
                colorScheme = selectedColorScheme,
                mushafType = MushafType.HAFS_1441,
                initialPage = 1,
                showNavigationControls = true,
                showPageInfo = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false }
            ) {
                ThemeSettingsSheet(
                    selectedReadingTheme = selectedTheme,
                    selectedColorScheme = selectedColorScheme,
                    onReadingThemeChange = { selectedTheme = it },
                    onColorSchemeChange = { selectedColorScheme = it },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedReciter?.nameArabic ?: "None",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = selectedReciter?.nameEnglish ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showPicker = true }) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
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

// ============================================================================
// MARK: - Settings Sheet
// ============================================================================

@Composable
fun ThemeSettingsSheet(
    selectedReadingTheme: ReadingTheme,
    selectedColorScheme: ColorSchemeType,
    onReadingThemeChange: (ReadingTheme) -> Unit,
    onColorSchemeChange: (ColorSchemeType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Theme Settings",
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

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
