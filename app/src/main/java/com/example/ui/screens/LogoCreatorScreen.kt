package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LogoBorderStyle
import com.example.data.model.LogoElementType
import com.example.data.model.LogoFontStyle
import com.example.data.model.LogoShape
import com.example.data.model.LogoTemplatePreset
import com.example.data.remote.DlsKitUrlPublisher
import com.example.rendering.DlsUvTextureExporter
import com.example.rendering.InteractiveLogoCanvas
import com.example.rendering.LogoBitmapExporter
import com.example.ui.components.ColorPaletteRow
import com.example.ui.viewmodel.LogoCreatorViewModel
import com.example.ui.viewmodel.LogoEditorTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogoCreatorScreen(
    viewModel: LogoCreatorViewModel,
    templates: List<LogoTemplatePreset>,
    onOpenMyDesigns: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var isGeneratingLogoUrl by remember { mutableStateOf(false) }
    var generatedLogoUrl by remember { mutableStateOf<String?>(null) }
    var generatedLogoHost by remember { mutableStateOf<String?>(null) }
    var logoUrlError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.spec) {
        generatedLogoUrl = null
        generatedLogoHost = null
        logoUrlError = null
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TEAM LOGO CREATOR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Drag elements directly on crest to position",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = viewModel::undo,
                    enabled = state.undoStack.isNotEmpty(),
                    modifier = Modifier.testTag("logo_undo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(
                    onClick = viewModel::redo,
                    enabled = state.redoStack.isNotEmpty(),
                    modifier = Modifier.testTag("logo_redo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
                IconButton(onClick = { viewModel.toggleGuideGrid(!state.showGuideGrid) }) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Toggle guide grid",
                        tint = if (state.showGuideGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenMyDesigns) {
                    Icon(
                        Icons.Default.FolderSpecial,
                        contentDescription = "My Logos",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.saveCurrentLogo { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    modifier = Modifier.testTag("quick_save_logo_button")
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Save Logo",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Interactive Drag-and-Drop Logo Stage
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveLogoCanvas(
                        spec = state.spec,
                        selectedLayerId = state.selectedLayerId,
                        showGuideGrid = state.showGuideGrid,
                        onLayerDragged = viewModel::updateLayerDrag,
                        onSelectLayer = viewModel::selectLayer,
                        modifier = Modifier
                            .size(200.dp)
                            .testTag("interactive_logo_canvas")
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            ScrollableTabRow(
                selectedTabIndex = LogoEditorTab.entries.indexOf(state.activeTab),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 12.dp
            ) {
                LogoEditorTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("logo_tab_${tab.name}")
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (state.activeTab) {
                    LogoEditorTab.TEMPLATES -> {
                        Text("Ready-Made Crest Templates", style = MaterialTheme.typography.titleSmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 8.dp)
                        ) {
                            items(templates, key = { it.id }) { preset ->
                                Card(
                                    modifier = Modifier
                                        .width(148.dp)
                                        .clickable { viewModel.applyTemplate(preset) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        InteractiveLogoCanvas(
                                            spec = preset.spec,
                                            selectedLayerId = null,
                                            modifier = Modifier.size(92.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(preset.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                        Text(
                                            preset.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LogoEditorTab.SHAPE -> {
                        Text("Crest Shape", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LogoShape.entries.forEach { shape ->
                                FilterChip(
                                    selected = state.spec.shape == shape,
                                    onClick = { viewModel.setShape(shape) },
                                    label = { Text(shape.label) },
                                    modifier = Modifier.testTag("logo_shape_${shape.name}")
                                )
                            }
                        }
                        ColorPaletteRow("Background Color", state.spec.bgColorHex, viewModel::setBgColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gradient Fill", style = MaterialTheme.typography.titleSmall)
                            Switch(checked = state.spec.useGradient, onCheckedChange = viewModel::setUseGradient)
                        }
                        if (state.spec.useGradient) {
                            ColorPaletteRow("Gradient Secondary Color", state.spec.secondaryColorHex, viewModel::setSecondaryColor)
                        }
                        Text("Border Style", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LogoBorderStyle.entries.forEach { bs ->
                                FilterChip(
                                    selected = state.spec.borderStyle == bs,
                                    onClick = { viewModel.setBorderStyle(bs) },
                                    label = { Text(bs.label) }
                                )
                            }
                        }
                        ColorPaletteRow("Border Color", state.spec.borderColorHex, viewModel::setBorderColor)
                    }

                    LogoEditorTab.ELEMENTS -> {
                        Text("Tap an element to add to crest", style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LogoElementType.entries.forEach { elem ->
                                OutlinedButton(
                                    onClick = { viewModel.addElement(elem) },
                                    modifier = Modifier.testTag("add_logo_elem_${elem.name}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(elem.label)
                                }
                            }
                        }
                    }

                    LogoEditorTab.LAYERS -> {
                        val layers = state.spec.layers
                        if (layers.isEmpty()) {
                            Text(
                                "No elements added yet. Add elements from the Elements tab.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text("Element Layers (Top to Bottom)", style = MaterialTheme.typography.titleSmall)
                            layers.reversed().forEach { layer ->
                                val selected = layer.id == state.selectedLayerId
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectLayer(layer.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = layer.type.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { viewModel.moveLayerOrder(layer.id, forward = true) }) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.moveLayerOrder(layer.id, forward = false) }) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.removeLayer(layer.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            val activeLayer = layers.firstOrNull { it.id == state.selectedLayerId }
                            if (activeLayer != null) {
                                ColorPaletteRow(
                                    label = "${activeLayer.type.label} Color",
                                    selectedHex = activeLayer.colorHex,
                                    onColorSelected = { hex -> viewModel.updateSelectedLayer { it.copy(colorHex = hex) } }
                                )
                                Text("Scale: ${(activeLayer.scale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = activeLayer.scale,
                                    onValueChange = { s -> viewModel.updateSelectedLayer { it.copy(scale = s) } },
                                    valueRange = 0.25f..1.8f
                                )
                                Text("Rotation: ${activeLayer.rotationDeg.toInt()}°", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = activeLayer.rotationDeg,
                                    onValueChange = { r -> viewModel.updateSelectedLayer { it.copy(rotationDeg = r) } },
                                    valueRange = -180f..180f
                                )
                                Text("Opacity: ${(activeLayer.opacity * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = activeLayer.opacity,
                                    onValueChange = { o -> viewModel.updateSelectedLayer { it.copy(opacity = o) } },
                                    valueRange = 0.1f..1f
                                )
                            }
                        }
                    }

                    LogoEditorTab.TEXT -> {
                        OutlinedTextField(
                            value = state.spec.clubName,
                            onValueChange = viewModel::setClubName,
                            label = { Text("Club Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logo_club_name_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = state.spec.initials,
                                onValueChange = viewModel::setInitials,
                                label = { Text("Initials") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.spec.establishedYear,
                                onValueChange = viewModel::setEstYear,
                                label = { Text("Est. Year") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text("Font Style", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LogoFontStyle.entries.forEach { font ->
                                FilterChip(
                                    selected = state.spec.fontStyle == font,
                                    onClick = { viewModel.setFontStyle(font) },
                                    label = { Text(font.label) }
                                )
                            }
                        }
                        ColorPaletteRow("Text Color", state.spec.textColorHex, viewModel::setTextColor)
                        Text("Text Vertical Position", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = state.spec.textOffsetY,
                            onValueChange = viewModel::setTextOffsetY,
                            valueRange = -0.32f..0.34f
                        )
                    }

                    LogoEditorTab.EXPORT -> {
                        OutlinedTextField(
                            value = state.designName,
                            onValueChange = viewModel::setDesignName,
                            label = { Text("Logo Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.saveCurrentLogo(asCopy = false) { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_logo_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save Logo", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.saveCurrentLogo(asCopy = true) { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FileCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save Copy")
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val bmp = LogoBitmapExporter.renderLogoBitmap(context, state.spec, pixelSize = 512, transparentBg = true)
                                    val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${state.designName}_dls_crest_512")
                                    if (res.success) {
                                        viewModel.recordExport()
                                        snackbarHostState.showSnackbar("Transparent 512×512 DLS Crest saved to ${res.displayPath}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_logo_transparent_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Export Transparent PNG (512×512 DLS)")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val bmp = LogoBitmapExporter.renderLogoBitmap(context, state.spec, pixelSize = 1024, transparentBg = true)
                                        val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${state.designName}_hd_1024")
                                        if (res.success) {
                                            viewModel.recordExport()
                                            snackbarHostState.showSnackbar("High-Res 1024×1024 PNG saved to ${res.displayPath}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("HD 1024px")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val bmp = LogoBitmapExporter.renderLogoBitmap(context, state.spec, pixelSize = 512, transparentBg = true)
                                        val res = DlsUvTextureExporter.exportPngAndShareableUri(context, bmp, "${state.designName}_share", saveToGallery = false)
                                        res.fileUri?.let { DlsUvTextureExporter.launchShareImageIntent(context, it, state.designName) }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share Logo")
                            }
                        }

                        OutlinedButton(
                            enabled = !isGeneratingLogoUrl,
                            onClick = {
                                isGeneratingLogoUrl = true
                                logoUrlError = null
                                scope.launch {
                                    val bmp = LogoBitmapExporter.renderLogoBitmap(context, state.spec, pixelSize = 512, transparentBg = true)
                                    val res = DlsKitUrlPublisher.publishPngForDls26(bmp, "${state.designName}_logo")
                                    isGeneratingLogoUrl = false
                                    if (res.success && res.url.isNotBlank()) {
                                        generatedLogoUrl = res.url
                                        generatedLogoHost = res.hostName
                                        clipboard.setText(AnnotatedString(res.url))
                                        viewModel.recordExport()
                                        snackbarHostState.showSnackbar("DLS 26 Logo URL generated & copied!")
                                    } else {
                                        logoUrlError = res.errorMessage ?: "Could not publish logo URL"
                                        snackbarHostState.showSnackbar(logoUrlError ?: "Upload failed")
                                    }
                                }
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_live_logo_url_button")
                        ) {
                            if (isGeneratingLogoUrl) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Publishing 512×512 Crest to DLS 26 URL...")
                            } else {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (generatedLogoUrl == null) "Generate Live DLS 26 Logo URL (.png)"
                                    else "Regenerate Live DLS 26 Logo URL"
                                )
                            }
                        }

                        if (!logoUrlError.isNullOrBlank()) {
                            Text(
                                text = logoUrlError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (!generatedLogoUrl.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Live DLS 26 Logo URL${generatedLogoHost?.let { " • $it" } ?: ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = generatedLogoUrl ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Button(
                                        onClick = {
                                            generatedLogoUrl?.let { url ->
                                                clipboard.setText(AnnotatedString(url))
                                                scope.launch { snackbarHostState.showSnackbar("Copied live DLS 26 Logo URL") }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Copy DLS 26 Logo URL")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
