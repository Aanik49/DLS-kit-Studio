package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavedKitEntity
import com.example.data.local.SavedLogoEntity
import com.example.data.model.ClubKitBundle
import com.example.data.model.JerseyDesignSpec
import com.example.data.model.LogoDesignSpec
import com.example.rendering.DlsUvTextureExporter
import com.example.rendering.InteractiveLogoCanvas
import com.example.rendering.JerseyPreviewCanvas
import com.example.rendering.LogoBitmapExporter
import com.example.ui.components.KitShowcaseCard
import com.example.ui.viewmodel.MainStudioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDesignsScreen(
    viewModel: MainStudioViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onEditKit: (SavedKitEntity, JerseyDesignSpec) -> Unit,
    onEditLogo: (SavedLogoEntity, LogoDesignSpec) -> Unit,
    onClubSelected: (ClubKitBundle) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val savedKits by viewModel.savedKits.collectAsStateWithLifecycle()
    val savedLogos by viewModel.savedLogos.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewed.collectAsStateWithLifecycle()
    val allClubs by viewModel.allClubs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    val tabs = listOf("My Kits", "My Logos", "Favorites", "Recently Viewed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Designs Library") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("my_designs_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, maxLines = 1, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("designs_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (savedKits.isEmpty()) {
                        EmptyDesignsState("No saved kits yet", "Design a custom jersey in Kit Creator and tap Save.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(savedKits, key = { it.id }) { kit ->
                                val spec = remember(kit.specJson) { viewModel.decodeKitSpec(kit.specJson) }
                                SavedKitCard(
                                    kit = kit,
                                    spec = spec,
                                    onEdit = { onEditKit(kit, spec) },
                                    onDuplicate = {
                                        viewModel.duplicateSavedKit(kit)
                                        scope.launch { snackbarHostState.showSnackbar("Duplicated ${kit.name}") }
                                    },
                                    onDelete = {
                                        viewModel.deleteSavedKit(kit.id)
                                        scope.launch { snackbarHostState.showSnackbar("Deleted ${kit.name}") }
                                    },
                                    onExport = {
                                        scope.launch {
                                            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, spec, false)
                                            val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${kit.name}_dls_512")
                                            if (res.success) {
                                                viewModel.recordExport()
                                                snackbarHostState.showSnackbar("Exported 512×512 DLS Kit to ${res.displayPath}")
                                            }
                                        }
                                    },
                                    onShare = {
                                        scope.launch {
                                            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, spec, false)
                                            val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${kit.name}_share", saveToGallery = false)
                                            res.fileUri?.let { DlsUvTextureExporter.launchShareImageIntent(context, it, kit.name) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (savedLogos.isEmpty()) {
                        EmptyDesignsState("No saved logos yet", "Create a club crest in Logo Creator and tap Save.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(savedLogos, key = { it.id }) { logo ->
                                val spec = remember(logo.specJson) { viewModel.decodeLogoSpec(logo.specJson) }
                                SavedLogoCard(
                                    logo = logo,
                                    spec = spec,
                                    onEdit = { onEditLogo(logo, spec) },
                                    onDuplicate = {
                                        viewModel.duplicateSavedLogo(logo)
                                        scope.launch { snackbarHostState.showSnackbar("Duplicated ${logo.name}") }
                                    },
                                    onDelete = {
                                        viewModel.deleteSavedLogo(logo.id)
                                        scope.launch { snackbarHostState.showSnackbar("Deleted ${logo.name}") }
                                    },
                                    onExport = {
                                        scope.launch {
                                            val bmp = LogoBitmapExporter.renderLogoBitmap(context, spec, 512, true)
                                            val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${logo.name}_512")
                                            if (res.success) {
                                                viewModel.recordExport()
                                                snackbarHostState.showSnackbar("Exported PNG to ${res.displayPath}")
                                            }
                                        }
                                    },
                                    onShare = {
                                        scope.launch {
                                            val bmp = LogoBitmapExporter.renderLogoBitmap(context, spec, 512, true)
                                            val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${logo.name}_share", saveToGallery = false)
                                            res.fileUri?.let { DlsUvTextureExporter.launchShareImageIntent(context, it, logo.name) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    val favClubs = favorites.mapNotNull { fav -> allClubs.firstOrNull { it.id == fav.clubId } }
                    if (favClubs.isEmpty()) {
                        EmptyDesignsState("No favorite clubs yet", "Tap the heart icon on any club kit to bookmark it here.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(favClubs, key = { it.id }) { club ->
                                KitShowcaseCard(
                                    club = club,
                                    onClick = { onClubSelected(club) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                3 -> {
                    val viewedClubs = recentlyViewed.mapNotNull { rv -> allClubs.firstOrNull { it.id == rv.clubId } }
                    if (viewedClubs.isEmpty()) {
                        EmptyDesignsState("No recently viewed clubs", "Browse any club kit from Home or Kits to track history.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewedClubs, key = { it.id }) { club ->
                                KitShowcaseCard(
                                    club = club,
                                    onClick = { onClubSelected(club) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDesignsState(title: String, tip: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FolderOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SavedKitCard(
    kit: SavedKitEntity,
    spec: JerseyDesignSpec,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JerseyPreviewCanvas(spec = spec, modifier = Modifier.size(82.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(kit.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    "${kit.clubName} • ${spec.slotType.shortLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.FileCopy, contentDescription = "Duplicate", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Download, contentDescription = "Export 512px", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedLogoCard(
    logo: SavedLogoEntity,
    spec: LogoDesignSpec,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InteractiveLogoCanvas(
                spec = spec,
                selectedLayerId = null,
                modifier = Modifier.size(82.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(logo.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    "${spec.clubName} • ${spec.shape.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.FileCopy, contentDescription = "Duplicate", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.Download, contentDescription = "Export PNG", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
