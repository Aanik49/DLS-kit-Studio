package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClubKitBundle
import com.example.data.model.JerseyViewAngle
import com.example.data.model.KitSlotType
import com.example.data.remote.DlsKitUrlPublisher
import com.example.rendering.ClubCrestBadge
import com.example.rendering.DlsUvTextureExporter
import com.example.rendering.JerseyPreviewCanvas
import com.example.rendering.KeeperGlovesPreview
import com.example.rendering.MatchdayPosterExporter
import com.example.rendering.StudioPreviewExporter
import com.example.ui.viewmodel.KitCreatorViewModel
import com.example.ui.viewmodel.KitEditorTab
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitCreatorScreen(
    viewModel: KitCreatorViewModel,
    catalogClubs: List<ClubKitBundle>,
    onOpenMyDesigns: () -> Unit,
    onOpenKitUploader: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showClubPickerSheet by remember { mutableStateOf(false) }
    var showPosterSheet by remember { mutableStateOf(false) }
    var isGeneratingUrl by remember { mutableStateOf(false) }
    var generatedKitUrl by remember { mutableStateOf<String?>(null) }
    var generatedUrlHost by remember { mutableStateOf<String?>(null) }
    var urlErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.spec) {
        generatedKitUrl = null
        generatedUrlHost = null
        urlErrorMessage = null
    }

    val clubLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.setCustomClubLogoUri(uri.toString())
                scope.launch { snackbarHostState.showSnackbar("Custom crest image applied") }
            }
        }
    )

    val sponsorLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.setCustomSponsorLogoUri(uri.toString())
                scope.launch { snackbarHostState.showSnackbar("Custom sponsor image applied") }
            }
        }
    )

    val customStickerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.setCustomStickerUri(uri.toString())
                scope.launch { snackbarHostState.showSnackbar("Custom PNG sticker overlay applied") }
            }
        }
    )

    val uvPreviewBitmap = remember(state.spec, state.showUvPreview) {
        if (state.showUvPreview) {
            DlsUvTextureExporter.generateDlsUvBitmap(context, state.spec, includeUvGuideOverlay = true)
        } else null
    }

    val posterBitmap = remember(state.spec, generatedKitUrl, state.season, showPosterSheet) {
        if (showPosterSheet) {
            MatchdayPosterExporter.generateMatchdayPosterBitmap(
                context = context,
                spec = state.spec,
                dlsKitUrl = generatedKitUrl,
                seasonLabel = state.season
            )
        } else null
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            val isCompactHeight = maxHeight < 580.dp
            val stageHeight = if (isCompactHeight) 118.dp else 185.dp
            val canvasHeight = if (isCompactHeight) 112.dp else 176.dp
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Studio Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_club_button")
                        .clickable { showClubPickerSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Select Club",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.spec.clubName.ifBlank { "Custom Club" },
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                            Text(
                                text = "Tap to switch club or custom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = viewModel::undo,
                    enabled = state.undoStack.isNotEmpty(),
                    modifier = Modifier.testTag("kit_undo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(
                    onClick = viewModel::redo,
                    enabled = state.redoStack.isNotEmpty(),
                    modifier = Modifier.testTag("kit_redo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
                IconButton(
                    onClick = onOpenMyDesigns,
                    modifier = Modifier.testTag("open_my_designs_button")
                ) {
                    Icon(
                        Icons.Default.FolderSpecial,
                        contentDescription = "My Designs",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.saveCurrentDesign { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    modifier = Modifier.testTag("quick_save_kit_button")
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Save Kit",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Live Jersey Stage
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val normAngle = ((state.rotationAngleDeg % 360f) + 360f) % 360f
                    val activeViewLabel = when {
                        state.showUvPreview -> "512×512 UV"
                        normAngle in 45f..135f -> "RIGHT ${normAngle.roundToInt()}°"
                        normAngle in 135f..225f -> "BACK ${normAngle.roundToInt()}°"
                        normAngle in 225f..315f -> "LEFT ${normAngle.roundToInt()}°"
                        else -> "FRONT ${normAngle.roundToInt()}°"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(stageHeight)
                            .clip(MaterialTheme.shapes.small)
                            .testTag("kit_360_stage")
                            .pointerInput(state.showUvPreview) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (!state.showUvPreview) {
                                        viewModel.rotateBy(dragAmount * 0.85f)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = state.showUvPreview,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "creatorStage"
                        ) { uv ->
                            if (uv && uvPreviewBitmap != null) {
                                Image(
                                    bitmap = uvPreviewBitmap.asImageBitmap(),
                                    contentDescription = "512x512 DLS UV Texture Preview",
                                    modifier = Modifier
                                        .size(canvasHeight)
                                        .graphicsLayer(scaleX = state.zoomLevel, scaleY = state.zoomLevel)
                                )
                            } else {
                                JerseyPreviewCanvas(
                                    spec = state.spec,
                                    isBackView = state.isBackView,
                                    showShortsPreview = state.showShortsPreview,
                                    rotationAngleDeg = state.rotationAngleDeg,
                                    modifier = Modifier
                                        .height(canvasHeight)
                                        .graphicsLayer(scaleX = state.zoomLevel, scaleY = state.zoomLevel)
                                )
                            }
                        }

                        // Slot & Live 360° Angle badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${state.spec.slotType.shortLabel} • $activeViewLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (!state.showUvPreview) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                            ) {
                                Text(
                                    text = "↔ Swipe 360°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            if (state.spec.slotType == KitSlotType.GOALKEEPER) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .testTag("stage_gk_gloves_badge"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        KeeperGlovesPreview(
                                            spec = state.spec,
                                            modifier = Modifier.width(56.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stage Controls: Front / Right / Back / Left / UV / Full Kit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !state.showUvPreview && (normAngle < 45f || normAngle > 315f),
                            onClick = { viewModel.snapToViewAngle(JerseyViewAngle.FRONT) },
                            label = { Text("Front") },
                            leadingIcon = {
                                Icon(Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("stage_front_chip")
                        )
                        FilterChip(
                            selected = !state.showUvPreview && normAngle in 45f..135f,
                            onClick = { viewModel.snapToViewAngle(JerseyViewAngle.RIGHT_SIDE) },
                            label = { Text("Right") },
                            modifier = Modifier.testTag("stage_right_chip")
                        )
                        FilterChip(
                            selected = !state.showUvPreview && normAngle in 135f..225f,
                            onClick = { viewModel.snapToViewAngle(JerseyViewAngle.BACK) },
                            label = { Text("Back") },
                            leadingIcon = {
                                Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("stage_back_chip")
                        )
                        FilterChip(
                            selected = !state.showUvPreview && normAngle in 225f..315f,
                            onClick = { viewModel.snapToViewAngle(JerseyViewAngle.LEFT_SIDE) },
                            label = { Text("Left") },
                            modifier = Modifier.testTag("stage_left_chip")
                        )
                        FilterChip(
                            selected = state.showUvPreview,
                            onClick = { viewModel.toggleUvPreview(!state.showUvPreview) },
                            label = { Text("UV") },
                            leadingIcon = {
                                Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.testTag("stage_uv_chip")
                        )
                        FilterChip(
                            selected = state.showShortsPreview,
                            onClick = { viewModel.toggleShortsPreview(!state.showShortsPreview) },
                            label = { Text("Full Kit") },
                            modifier = Modifier.testTag("stage_full_kit_chip")
                        )
                    }

                    if (!isCompactHeight) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Slider(
                                value = state.zoomLevel,
                                onValueChange = viewModel::setZoom,
                                valueRange = 0.8f..1.65f,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                            )
                            Text(
                                text = "${(state.zoomLevel * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Editor Category Tabs
            ScrollableTabRow(
                selectedTabIndex = KitEditorTab.entries.indexOf(state.activeTab),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 12.dp
            ) {
                KitEditorTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        modifier = Modifier.testTag("kit_tab_${tab.name}")
                    )
                }
            }

            // Tool Content Area
            if (state.activeTab == KitEditorTab.PATTERN) {
                KitPatternPanel(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    when (state.activeTab) {
                        KitEditorTab.BASE -> KitBasePanel(state, viewModel)
                        KitEditorTab.GK_STUDIO -> KitGkStudioPanel(state, viewModel)
                        KitEditorTab.COLORS -> KitColorsPanel(state, viewModel)
                        KitEditorTab.PATTERN -> Unit
                        KitEditorTab.DETAILS -> KitDetailsPanel(state, viewModel)
                        KitEditorTab.BRANDING -> KitBrandingPanel(
                            state = state,
                            viewModel = viewModel,
                            onPickClubLogo = {
                                clubLogoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onPickSponsorLogo = {
                                sponsorLogoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onPickCustomSticker = {
                                customStickerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        KitEditorTab.EXPORT -> KitExportPanel(
                            state = state,
                            viewModel = viewModel,
                            isGeneratingUrl = isGeneratingUrl,
                            generatedKitUrl = generatedKitUrl,
                            generatedUrlHost = generatedUrlHost,
                            urlErrorMessage = urlErrorMessage,
                            onSave = {
                                viewModel.saveCurrentDesign(asCopy = false) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onDuplicate = {
                                viewModel.saveCurrentDesign(asCopy = true) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onExportDlsUv512 = {
                                scope.launch {
                                    val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, state.spec, false)
                                    val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                        context, bmp, "${state.designName}_dls_512"
                                    )
                                    if (res.success) {
                                        viewModel.recordExport()
                                        snackbarHostState.showSnackbar("512×512 DLS UV PNG saved to ${res.displayPath}")
                                    } else {
                                        snackbarHostState.showSnackbar(res.errorMessage ?: "Export failed")
                                    }
                                }
                            },
                            onExportPreviewPng = {
                                scope.launch {
                                    val bmp = StudioPreviewExporter.generateJerseyShowcaseBitmap(context, state.spec)
                                    val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                        context, bmp, "${state.designName}_preview"
                                    )
                                    if (res.success) {
                                        viewModel.recordExport()
                                        snackbarHostState.showSnackbar("Jersey Showcase PNG saved to ${res.displayPath}")
                                    }
                                }
                            },
                            onShareKit = {
                                scope.launch {
                                    val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, state.spec, false)
                                    val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                        context, bmp, "${state.designName}_dls_share", saveToGallery = false
                                    )
                                    res.fileUri?.let { uri ->
                                        DlsUvTextureExporter.launchShareImageIntent(context, uri, state.designName)
                                    }
                                }
                            },
                            onGenerateLiveDlsUrl = {
                                isGeneratingUrl = true
                                urlErrorMessage = null
                                scope.launch {
                                    val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, state.spec, false)
                                    val res = DlsKitUrlPublisher.publishPngForDls26(bmp, state.designName)
                                    isGeneratingUrl = false
                                    if (res.success && res.url.isNotBlank()) {
                                        generatedKitUrl = res.url
                                        generatedUrlHost = res.hostName
                                        clipboard.setText(AnnotatedString(res.url))
                                        viewModel.recordExport()
                                        snackbarHostState.showSnackbar("DLS 26 Kit URL generated & copied!")
                                    } else {
                                        urlErrorMessage = res.errorMessage ?: "Could not generate URL"
                                        snackbarHostState.showSnackbar(urlErrorMessage ?: "Upload failed")
                                    }
                                }
                            },
                            onCopyLiveDlsUrl = { url ->
                                clipboard.setText(AnnotatedString(url))
                                scope.launch { snackbarHostState.showSnackbar("Copied live DLS 26 URL to clipboard") }
                            },
                            onOpenKitUploader = onOpenKitUploader,
                            onOpenMatchdayPoster = { showPosterSheet = true }
                        )
                    }
                }
            }
        }
        }
    }

    if (showPosterSheet && posterBitmap != null) {
        MatchdayPosterPreviewSheet(
            posterBitmap = posterBitmap,
            dlsKitUrl = generatedKitUrl,
            isPublishingUrl = isGeneratingUrl,
            onDismiss = { showPosterSheet = false },
            onPublishUrlForQr = {
                isGeneratingUrl = true
                urlErrorMessage = null
                scope.launch {
                    val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, state.spec, false)
                    val res = DlsKitUrlPublisher.publishPngForDls26(bmp, state.designName)
                    isGeneratingUrl = false
                    if (res.success && res.url.isNotBlank()) {
                        generatedKitUrl = res.url
                        generatedUrlHost = res.hostName
                        clipboard.setText(AnnotatedString(res.url))
                        snackbarHostState.showSnackbar("Live DLS 26 URL bound to Poster QR!")
                    } else {
                        urlErrorMessage = res.errorMessage ?: "Could not publish URL"
                        snackbarHostState.showSnackbar(urlErrorMessage ?: "Upload failed")
                    }
                }
            },
            onSavePoster = {
                scope.launch {
                    val res = DlsUvTextureExporter.exportPngAndShareableUri(
                        context = context,
                        bitmap = posterBitmap,
                        filePrefix = "${state.designName}_matchday_poster",
                        saveToGallery = true
                    )
                    if (res.success) {
                        viewModel.recordExport()
                        snackbarHostState.showSnackbar("Matchday Poster saved to ${res.displayPath}")
                    } else {
                        snackbarHostState.showSnackbar(res.errorMessage ?: "Poster export failed")
                    }
                }
            },
            onSharePoster = {
                scope.launch {
                    val res = DlsUvTextureExporter.exportPngAndShareableUri(
                        context = context,
                        bitmap = posterBitmap,
                        filePrefix = "${state.designName}_matchday_story",
                        saveToGallery = false
                    )
                    res.fileUri?.let { uri ->
                        DlsUvTextureExporter.launchShareImageIntent(context, uri, "${state.designName} Matchday Poster")
                    }
                }
            }
        )
    }

    if (showClubPickerSheet) {
        ClubSelectorBottomSheet(
            clubs = catalogClubs,
            onDismiss = { showClubPickerSheet = false },
            onSelectClub = { club ->
                viewModel.loadFromClub(club, 0)
                showClubPickerSheet = false
            },
            onCreateCustomClub = { customName ->
                viewModel.startFreshCustomClub(customName)
                showClubPickerSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubSelectorBottomSheet(
    clubs: List<ClubKitBundle>,
    onDismiss: () -> Unit,
    onSelectClub: (ClubKitBundle) -> Unit,
    onCreateCustomClub: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedLeague by remember { mutableStateOf("All") }
    var customClubInput by remember { mutableStateOf("") }

    val leagues = remember(clubs) {
        listOf("All") + clubs.map { it.league }.distinct()
    }
    val filtered = remember(clubs, query, selectedLeague) {
        clubs.filter { c ->
            (selectedLeague == "All" || c.league == selectedLeague) &&
                (query.isBlank() || c.clubName.contains(query, true) || c.country.contains(query, true))
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select Club Base or Create Custom",
                style = MaterialTheme.typography.headlineMedium
            )

            // Custom Club quick creation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customClubInput,
                    onValueChange = { customClubInput = it },
                    placeholder = { Text("New custom club name...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onCreateCustomClub(customClubInput) },
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Custom")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search clubs or countries...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(leagues) { league ->
                    FilterChip(
                        selected = league == selectedLeague,
                        onClick = { selectedLeague = league },
                        label = { Text(league) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filtered, key = { it.id }) { club ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectClub(club) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClubCrestBadge(
                                primaryHex = club.crestPrimaryHex,
                                secondaryHex = club.crestSecondaryHex,
                                shortCode = club.shortCode,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(club.clubName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${club.league} • ${club.country} (${club.season})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { onSelectClub(club) }) {
                                Text("Load")
                            }
                        }
                    }
                }
            }
        }
    }
}
