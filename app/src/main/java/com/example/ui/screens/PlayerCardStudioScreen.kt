package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rendering.DlsPlayerCardExporter
import com.example.rendering.DlsUvTextureExporter
import com.example.ui.viewmodel.PlayerCardTab
import com.example.ui.viewmodel.PlayerCardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCardStudioScreen(
    viewModel: PlayerCardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.setCustomPhotoUri(uri.toString())
                scope.launch { snackbarHostState.showSnackbar("Custom player photo applied") }
            }
        }
    )

    val previewBitmap = remember(state.activeCard, state.shieldOnlyMode) {
        if (state.shieldOnlyMode) {
            DlsPlayerCardExporter.renderShieldOnlyBitmap(context, state.activeCard, width = 540, height = 720)
        } else {
            DlsPlayerCardExporter.renderFullStatsCardBitmap(context, state.activeCard, width = 960, height = 570)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DLS PLAYER CARD STUDIO",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "100-Player Database • Custom Card & PNG Download",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_card_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createNewCustomPlayer()
                            scope.launch { snackbarHostState.showSnackbar("Started new custom player card") }
                        },
                        modifier = Modifier.testTag("player_card_new_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                // Live DLS Player Card Preview Stage
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (state.shieldOnlyMode) 196.dp else 188.dp)
                                .testTag("player_card_live_preview"),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Live DLS Player Card Preview for ${state.activeCard.fullName}",
                                modifier = if (state.shieldOnlyMode) {
                                    Modifier
                                        .height(190.dp)
                                        .aspectRatio(0.75f)
                                } else {
                                    Modifier
                                        .height(182.dp)
                                        .aspectRatio(1280f / 760f)
                                        .clip(RoundedCornerShape(10.dp))
                                }
                            )
                        }

                        // Card Format Toggle + Download & Share PNG Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !state.shieldOnlyMode,
                                onClick = { viewModel.setShieldOnlyMode(false) },
                                label = { Text("Full Card", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("card_mode_full")
                            )
                            FilterChip(
                                selected = state.shieldOnlyMode,
                                onClick = { viewModel.setShieldOnlyMode(true) },
                                label = { Text("Shield PNG", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("card_mode_shield")
                            )
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val hiRes = if (state.shieldOnlyMode) {
                                            DlsPlayerCardExporter.renderShieldOnlyBitmap(context, state.activeCard)
                                        } else {
                                            DlsPlayerCardExporter.renderFullStatsCardBitmap(context, state.activeCard)
                                        }
                                        val suffix = if (state.shieldOnlyMode) "dls_shield" else "dls_card"
                                        val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                            context = context,
                                            bitmap = hiRes,
                                            filePrefix = "${state.activeCard.cardShortName}_$suffix",
                                            saveToGallery = true
                                        )
                                        hiRes.recycle()
                                        if (res.success) {
                                            snackbarHostState.showSnackbar("Player Card PNG saved to ${res.displayPath}")
                                        } else {
                                            snackbarHostState.showSnackbar(res.errorMessage ?: "Export failed")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("download_player_card_png_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PNG", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val hiRes = if (state.shieldOnlyMode) {
                                            DlsPlayerCardExporter.renderShieldOnlyBitmap(context, state.activeCard)
                                        } else {
                                            DlsPlayerCardExporter.renderFullStatsCardBitmap(context, state.activeCard)
                                        }
                                        val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                            context = context,
                                            bitmap = hiRes,
                                            filePrefix = "${state.activeCard.cardShortName}_share",
                                            saveToGallery = false
                                        )
                                        hiRes.recycle()
                                        res.fileUri?.let { uri ->
                                            DlsUvTextureExporter.launchShareImageIntent(
                                                context,
                                                uri,
                                                "${state.activeCard.fullName} DLS Card"
                                            )
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("share_player_card_png_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Player Card PNG", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // 3 Tabs: 100 Players | Card & Photo | 8 Stats
                TabRow(
                    selectedTabIndex = PlayerCardTab.entries.indexOf(state.activeTab),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    PlayerCardTab.entries.forEach { tab ->
                        Tab(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.setActiveTab(tab) },
                            text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                            modifier = Modifier.testTag("player_card_tab_${tab.name}")
                        )
                    }
                }

                // Tab Body
                when (state.activeTab) {
                    PlayerCardTab.DATABASE -> {
                        PlayerDatabaseBrowserPanel(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                    PlayerCardTab.IDENTITY -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            PlayerCardIdentityPanel(
                                card = state.activeCard,
                                viewModel = viewModel,
                                onPickPhoto = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onSaveToDb = {
                                    val msg = viewModel.saveActiveCardToDatabase()
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
                    }
                    PlayerCardTab.STATS -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            PlayerCardStatsPanel(
                                card = state.activeCard,
                                viewModel = viewModel,
                                onSaveToDb = {
                                    val msg = viewModel.saveActiveCardToDatabase()
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
