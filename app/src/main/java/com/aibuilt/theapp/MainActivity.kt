package com.aibuilt.theapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Data
// ---------------------------------------------------------------------------

data class Reel(
    val id: String,
    val title: String,
    val creator: String,
    val description: String,
    val videoUrl: String,
    val runtime: String,
    val tags: List<String>,
    val accent: Color,
)

object SampleReels {

    private const val BASE = "https://storage.googleapis.com/gtv-videos-bucket/sample/"

    val all: List<Reel> = listOf(
        Reel(
            id = "reel-01",
            title = "Big Buck Bunny",
            creator = "@blenderstudio",
            description = "A giant rabbit with a heart of gold takes on three bullying rodents in this classic open movie.",
            videoUrl = BASE + "BigBuckBunny.mp4",
            runtime = "9:56",
            tags = listOf("animation", "comedy", "openmovie"),
            accent = Color(0xFFFF6B6B),
        ),
        Reel(
            id = "reel-02",
            title = "Elephants Dream",
            creator = "@orangefilm",
            description = "Two strange characters explore a mechanical world that rebuilds itself around them.",
            videoUrl = BASE + "ElephantsDream.mp4",
            runtime = "10:53",
            tags = listOf("surreal", "3d", "classic"),
            accent = Color(0xFF6FD3FF),
        ),
        Reel(
            id = "reel-03",
            title = "For Bigger Blazes",
            creator = "@chromecast",
            description = "Short, loud and full of fire — a quick demo clip with a punchy soundtrack.",
            videoUrl = BASE + "ForBiggerBlazes.mp4",
            runtime = "0:15",
            tags = listOf("short", "demo"),
            accent = Color(0xFFFFB74D),
        ),
        Reel(
            id = "reel-04",
            title = "For Bigger Escapes",
            creator = "@chromecast",
            description = "A quick escape sequence that shows off crisp motion and deep contrast.",
            videoUrl = BASE + "ForBiggerEscapes.mp4",
            runtime = "0:15",
            tags = listOf("short", "action"),
            accent = Color(0xFF81C784),
        ),
        Reel(
            id = "reel-05",
            title = "For Bigger Fun",
            creator = "@chromecast",
            description = "Bright colours, fast cuts and a very catchy loop.",
            videoUrl = BASE + "ForBiggerFun.mp4",
            runtime = "0:60",
            tags = listOf("fun", "loop"),
            accent = Color(0xFFBA68C8),
        ),
        Reel(
            id = "reel-06",
            title = "For Bigger Joyrides",
            creator = "@chromecast",
            description = "A road trip montage with plenty of engine noise and open sky.",
            videoUrl = BASE + "ForBiggerJoyrides.mp4",
            runtime = "0:15",
            tags = listOf("cars", "travel"),
            accent = Color(0xFF4DD0E1),
        ),
        Reel(
            id = "reel-07",
            title = "Sintel",
            creator = "@durian",
            description = "A lone girl and her dragon companion search for a lost friend across frozen lands.",
            videoUrl = BASE + "Sintel.mp4",
            runtime = "14:48",
            tags = listOf("fantasy", "animation"),
            accent = Color(0xFF9575CD),
        ),
        Reel(
            id = "reel-08",
            title = "Tears of Steel",
            creator = "@mango",
            description = "A sci-fi short about a group of warriors trying to fix a broken past in Amsterdam.",
            videoUrl = BASE + "TearsOfSteel.mp4",
            runtime = "12:14",
            tags = listOf("scifi", "vfx"),
            accent = Color(0xFFF06292),
        ),
    )
}

// ---------------------------------------------------------------------------
// Persistence (DataStore preferences)
// ---------------------------------------------------------------------------

private val Context.reelhouseStore: DataStore<Preferences> by preferencesDataStore(name = "reelhouse_prefs")

class ReelStore(private val context: Context) {

    private val favoritesKey = stringSetPreferencesKey("favorite_reel_ids")
    private val watchedKey = stringSetPreferencesKey("watched_reel_ids")

    val favorites: Flow<Set<String>> =
        context.reelhouseStore.data.map { prefs -> prefs[favoritesKey] ?: emptySet() }

    val watched: Flow<Set<String>> =
        context.reelhouseStore.data.map { prefs -> prefs[watchedKey] ?: emptySet() }

    suspend fun toggleFavorite(reelId: String) {
        context.reelhouseStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (reelId in current) current - reelId else current + reelId
        }
    }

    suspend fun markWatched(reelId: String) {
        context.reelhouseStore.edit { prefs ->
            val current = prefs[watchedKey] ?: emptySet()
            prefs[watchedKey] = current + reelId
        }
    }

    suspend fun clearWatched() {
        context.reelhouseStore.edit { prefs -> prefs.remove(watchedKey) }
    }
}

// ---------------------------------------------------------------------------
// Theme
// ---------------------------------------------------------------------------

private val ReelhouseDarkScheme = darkColorScheme(
    primary = Color(0xFFFF5C7A),
    onPrimary = Color(0xFF2A0A12),
    secondary = Color(0xFF6FD3FF),
    onSecondary = Color(0xFF00202B),
    background = Color(0xFF0D0D11),
    onBackground = Color(0xFFECECF1),
    surface = Color(0xFF15151B),
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF22222C),
    onSurfaceVariant = Color(0xFFB9B9C4),
)

@Composable
fun ReelhouseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ReelhouseDarkScheme, content = content)
}

// ---------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReelhouseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ReelhouseApp()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// App shell
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelhouseApp() {
    val context = LocalContext.current
    val store = remember { ReelStore(context.applicationContext) }
    val favorites by store.favorites.collectAsStateWithLifecycle(initialValue = emptySet<String>())
    val watched by store.watched.collectAsStateWithLifecycle(initialValue = emptySet<String>())
    val scope = rememberCoroutineScope()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val openReel = openId?.let { id -> SampleReels.all.firstOrNull { it.id == id } }

    BackHandler(enabled = openReel != null) { openId = null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = openReel?.title
                            ?: if (tab == 0) "Reelhouse" else "Saved reels",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (openReel != null) {
                        IconButton(onClick = { openId = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (openReel == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Feed") },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                        label = { Text("Saved") },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                openReel != null -> ReelPlayerScreen(
                    reel = openReel,
                    isFavorite = openReel.id in favorites,
                    onToggleFavorite = { scope.launch { store.toggleFavorite(openReel.id) } },
                    upNext = SampleReels.all.filter { it.id != openReel.id }.take(5),
                    onSelect = { id -> openId = id },
                    onMarkWatched = { scope.launch { store.markWatched(openReel.id) } },
                )

                tab == 0 -> FeedScreen(
                    query = query,
                    onQueryChange = { query = it },
                    favorites = favorites,
                    watched = watched,
                    onOpen = { id -> openId = id },
                    onToggleFavorite = { id -> scope.launch { store.toggleFavorite(id) } },
                )

                else -> SavedScreen(
                    favorites = favorites,
                    watched = watched,
                    onOpen = { id -> openId = id },
                    onToggleFavorite = { id -> scope.launch { store.toggleFavorite(id) } },
                    onBrowse = { tab = 0 },
                    onClearHistory = { scope.launch { store.clearWatched() } },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Feed
// ---------------------------------------------------------------------------

@Composable
fun FeedScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    favorites: Set<String>,
    watched: Set<String>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val results = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            SampleReels.all
        } else {
            SampleReels.all.filter { reel ->
                reel.title.lowercase().contains(q) ||
                    reel.creator.lowercase().contains(q) ||
                    reel.description.lowercase().contains(q) ||
                    reel.tags.any { tag -> tag.contains(q) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Search reels") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
            )
        }

        item {
            Text(
                text = if (query.isBlank()) "Trending now" else "Results for \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (results.isEmpty()) {
            item {
                Text(
                    text = "Nothing matched that search. Try another keyword.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(results, key = { reel -> reel.id }) { reel ->
                ReelCard(
                    reel = reel,
                    isFavorite = reel.id in favorites,
                    isWatched = reel.id in watched,
                    onOpen = { onOpen(reel.id) },
                    onToggleFavorite = { onToggleFavorite(reel.id) },
                )
            }
        }

        item {
            Text(
                text = "${SampleReels.all.size} reels available offline-friendly over the network.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelCard(
    reel: Reel,
    isFavorite: Boolean,
    isWatched: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                reel.accent,
                                reel.accent.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play ${reel.title}",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = reel.runtime,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (isWatched) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "watched",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reel.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = reel.creator,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = reel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        reel.tags.forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from saved" else "Save reel",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Saved
// ---------------------------------------------------------------------------

@Composable
fun SavedScreen(
    favorites: Set<String>,
    watched: Set<String>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBrowse: () -> Unit,
    onClearHistory: () -> Unit,
) {
    val saved = remember(favorites) { SampleReels.all.filter { it.id in favorites } }

    if (saved.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No saved reels yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap the heart on any reel to keep it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBrowse) { Text("Browse the feed") }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${saved.size} saved • ${watched.size} watched",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    if (watched.isNotEmpty()) {
                        Button(onClick = onClearHistory) { Text("Clear") }
                    }
                }
            }

            items(saved, key = { reel -> reel.id }) { reel ->
                ReelCard(
                    reel = reel,
                    isFavorite = true,
                    isWatched = reel.id in watched,
                    onOpen = { onOpen(reel.id) },
                    onToggleFavorite = { onToggleFavorite(reel.id) },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Player screen
// ---------------------------------------------------------------------------

@Composable
fun ReelPlayerScreen(
    reel: Reel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    upNext: List<Reel>,
    onSelect: (String) -> Unit,
    onMarkWatched: () -> Unit,
) {
    LaunchedEffect(reel.id) { onMarkWatched() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
            ) {
                VideoPlayer(
                    url = reel.videoUrl,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = reel.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${reel.creator} • ${reel.runtime}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = reel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    reel.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "#$tag",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isFavorite) "Saved" else "Save reel")
                }
            }
        }

        item {
            Text(
                text = "Up next",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(upNext, key = { next -> next.id }) { next ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onSelect(next.id) },
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(next.accent, next.accent.copy(alpha = 0.3f))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = next.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${next.creator} • ${next.runtime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ---------------------------------------------------------------------------
// ExoPlayer wrapper
// ---------------------------------------------------------------------------

@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> player.pause()
                Lifecycle.Event.ON_START -> player.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                setShowNextButton(false)
                setShowPreviousButton(false)
                setKeepScreenOn(true)
            }
        },
    )
}
