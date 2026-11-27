package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.data.remote.DlsKitUrlPublisher
import com.example.data.remote.PublishedKitUrlResult
import com.example.rendering.ClubCrestBadge
import com.example.rendering.DlsUvTextureExporter
import com.example.rendering.JerseyPreviewCanvas
import com.example.rendering.StudioPreviewExporter
import com.example.ui.viewmodel.MainStudioViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitDetailScreen(
    club: ClubKitBundle,
    viewModel: MainStudioViewModel,
    onBack: () -> Unit,
    onRemixInCreator: (ClubKitBundle, Int) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isFavorite by viewModel.observeIsFavorite(club.id).collectAsStateWithLifecycle(initialValue = false)
    val publishedUrlMap by viewModel.publishedUrls.collectAsStateWithLifecycle()
    var selectedSlotIndex by remember { mutableIntStateOf(0) }
    var rotationAngleDeg by remember { mutableFloatStateOf(0f) }
    var showUvSheet by remember { mutableStateOf(false) }
    var isGeneratingUrl by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(club.id) {
        viewModel.markClubViewed(club)
    }

    val variant = club.variants.getOrElse(selectedSlotIndex) { club.variants.first() }
    val cacheKey = "${club.id}_$selectedSlotIndex"
    val publishedResult = publishedUrlMap[cacheKey]
    val effectiveUrl = publishedResult?.url?.takeIf { it.isNotBlank() } ?: variant.kitUrl

    LaunchedEffect(club.id, selectedSlotIndex) {
        urlError = null
        if (publishedUrlMap[cacheKey]?.url.isNullOrBlank()) {
            isGeneratingUrl = true
            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, variant.designSpec, false)
            val result = DlsKitUrlPublisher.publishPngForDls26(
                bmp,
                "${club.shortCode}_${variant.slotType.shortLabel}"
            )
            isGeneratingUrl = false
            if (result.success && result.url.isNotBlank()) {
                viewModel.cachePublishedUrl(club.id, selectedSlotIndex, result)
            } else {
                urlError = result.errorMessage
            }
        }
    }

    val uvBitmap = remember(variant.designSpec, showUvSheet) {
        if (showUvSheet) DlsUvTextureExporter.generateDlsUvBitmap(context, variant.designSpec, true)
        else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = club.clubName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(club, isFavorite)
                        },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, variant.designSpec, false)
                                val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                    context, bmp, "${club.shortCode}_${variant.slotType.shortLabel}", saveToGallery = false
                                )
                                res.fileUri?.let { uri ->
                                    DlsUvTextureExporter.launchShareImageIntent(context, uri, "${club.clubName} ${variant.label}")
                                }
                            }
                        },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share kit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Club Info Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClubCrestBadge(
                        primaryHex = club.crestPrimaryHex,
                        secondaryHex = club.crestSecondaryHex,
                        shortCode = club.shortCode,
                        clubName = club.clubName,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = club.clubName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${club.league} • ${club.country} • ${club.season}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Home / Away / Third / GK Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    club.variants.forEachIndexed { idx, v ->
                        FilterChip(
                            selected = idx == selectedSlotIndex,
                            onClick = { selectedSlotIndex = idx },
                            label = { Text(v.slotType.shortLabel) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slot_chip_${v.slotType.shortLabel}")
                        )
                    }
                }
            }

            // Live Interactive Stage
            item {
                Card(
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val normAngle = ((rotationAngleDeg % 360f) + 360f) % 360f
                        val angleText = when {
                            showUvSheet -> "512×512 UV"
                            normAngle in 45f..135f -> "RIGHT ${normAngle.roundToInt()}°"
                            normAngle in 135f..225f -> "BACK ${normAngle.roundToInt()}°"
                            normAngle in 225f..315f -> "LEFT ${normAngle.roundToInt()}°"
                            else -> "FRONT ${normAngle.roundToInt()}°"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(showUvSheet) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (!showUvSheet) {
                                            rotationAngleDeg = (((rotationAngleDeg + dragAmount * 0.85f) % 360f) + 360f) % 360f
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = Pair(selectedSlotIndex, showUvSheet),
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "kitDetailPreview"
                            ) { (_, uv) ->
                                if (uv && uvBitmap != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(
                                            bitmap = uvBitmap.asImageBitmap(),
                                            contentDescription = "512x512 DLS UV Kit Texture",
                                            modifier = Modifier
                                                .size(260.dp)
                                                .clip(MaterialTheme.shapes.small)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "512×512 DLS UV Sprite Sheet",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    JerseyPreviewCanvas(
                                        spec = variant.designSpec,
                                        isBackView = normAngle in 135f..225f,
                                        showShortsPreview = true,
                                        rotationAngleDeg = rotationAngleDeg,
                                        modifier = Modifier.height(275.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.align(Alignment.TopStart),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = angleText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (!showUvSheet) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd),
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
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !showUvSheet && (normAngle < 45f || normAngle > 315f),
                                onClick = {
                                    showUvSheet = false
                                    rotationAngleDeg = JerseyViewAngle.FRONT.degrees
                                },
                                label = { Text("Front") }
                            )
                            FilterChip(
                                selected = !showUvSheet && normAngle in 45f..135f,
                                onClick = {
                                    showUvSheet = false
                                    rotationAngleDeg = JerseyViewAngle.RIGHT_SIDE.degrees
                                },
                                label = { Text("Right") }
                            )
                            FilterChip(
                                selected = !showUvSheet && normAngle in 135f..225f,
                                onClick = {
                                    showUvSheet = false
                                    rotationAngleDeg = JerseyViewAngle.BACK.degrees
                                },
                                label = { Text("Back") }
                            )
                            FilterChip(
                                selected = !showUvSheet && normAngle in 225f..315f,
                                onClick = {
                                    showUvSheet = false
                                    rotationAngleDeg = JerseyViewAngle.LEFT_SIDE.degrees
                                },
                                label = { Text("Left") }
                            )
                            FilterChip(
                                selected = showUvSheet,
                                onClick = { showUvSheet = !showUvSheet },
                                label = { Text("512×512 UV") },
                                leadingIcon = {
                                    Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }
            }

            // Kit Info & URL / Import Code Card
            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Kit Details & Import URL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = club.info,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (effectiveUrl.isNotBlank()) {
                                        "Live DLS 26 Kit URL (${variant.slotType.shortLabel})${publishedResult?.hostName?.let { " • $it" } ?: ""}"
                                    } else {
                                        "DLS 26 Kit URL (${variant.slotType.shortLabel})"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = effectiveUrl.ifBlank {
                                        "Tap 'Generate DLS 26 URL' below to host the 512×512 UV sprite sheet as a direct HTTPS .png link ready to paste into DLS 26."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (effectiveUrl.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (urlError != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = urlError ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Kit Preset ID: ${variant.importCode}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                enabled = !isGeneratingUrl,
                                onClick = {
                                    if (effectiveUrl.isNotBlank()) {
                                        clipboard.setText(AnnotatedString(effectiveUrl))
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Copied live DLS 26 URL to clipboard")
                                        }
                                    } else {
                                        val slotAtClick = selectedSlotIndex
                                        isGeneratingUrl = true
                                        urlError = null
                                        scope.launch {
                                            val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, variant.designSpec, false)
                                            val result = DlsKitUrlPublisher.publishPngForDls26(
                                                bmp,
                                                "${club.shortCode}_${variant.slotType.shortLabel}"
                                            )
                                            isGeneratingUrl = false
                                            if (result.success && result.url.isNotBlank()) {
                                                viewModel.cachePublishedUrl(club.id, slotAtClick, result)
                                                clipboard.setText(AnnotatedString(result.url))
                                                viewModel.recordExport()
                                                snackbarHostState.showSnackbar("DLS 26 Kit URL generated & copied!")
                                            } else {
                                                urlError = result.errorMessage ?: "Upload failed"
                                                snackbarHostState.showSnackbar(urlError ?: "Failed to generate URL")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_kit_url_button")
                            ) {
                                if (isGeneratingUrl) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Publishing...")
                                } else {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (effectiveUrl.isNotBlank()) "Copy DLS 26 URL" else "Generate DLS 26 URL")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val bmp = DlsUvTextureExporter.generateDlsUvBitmap(context, variant.designSpec, false)
                                        val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                            context, bmp, "${club.shortCode}_${variant.slotType.shortLabel}_dls_512"
                                        )
                                        if (res.success) {
                                            viewModel.recordExport()
                                            snackbarHostState.showSnackbar("Exported 512×512 DLS Kit to ${res.displayPath}")
                                        } else {
                                            snackbarHostState.showSnackbar(res.errorMessage ?: "Export failed")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_dls_kit_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Export 512px")
                            }
                        }
                    }
                }
            }

            // Remix + Preview PNG Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onRemixInCreator(club, selectedSlotIndex) },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("remix_in_studio_button")
                    ) {
                        Icon(Icons.Default.Brush, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Customize This Kit in Studio", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val bmp = StudioPreviewExporter.generateJerseyShowcaseBitmap(context, variant.designSpec)
                                val res = DlsUvTextureExporter.exportPngAndShareableUri(
                                    context, bmp, "${club.shortCode}_${variant.slotType.shortLabel}_preview"
                                )
                                if (res.success) {
                                    viewModel.recordExport()
                                    snackbarHostState.showSnackbar("Saved Jersey Preview PNG to ${res.displayPath}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export High-Res Jersey Preview PNG")
                    }
                }
            }
        }
    }
}
