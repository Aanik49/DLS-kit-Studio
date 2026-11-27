package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.DlsKitUrlPublisher
import com.example.rendering.DlsKitUploadAnalysis
import com.example.rendering.DlsUvTextureExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeneratedUploadLinkEntry(
    val title: String,
    val url: String,
    val hostName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlsKitUploaderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var analysis by remember { mutableStateOf<DlsKitUploadAnalysis?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var applyCleanCutout by remember { mutableStateOf(false) }
    var showGuideOverlay by remember { mutableStateOf(true) }
    var kitTitleInput by remember { mutableStateOf("Custom DLS 26 Kit") }

    var isUploading by remember { mutableStateOf(false) }
    var generatedUrl by remember { mutableStateOf<String?>(null) }
    var generatedHost by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sessionLinks = remember { mutableStateListOf<GeneratedUploadLinkEntry>() }

    fun handleSelectedUri(uri: Uri?) {
        if (uri == null) return
        isAnalyzing = true
        generatedUrl = null
        generatedHost = null
        errorMessage = null
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                DlsUvTextureExporter.analyzeUploadedDlsKit(context, uri)
            }
            isAnalyzing = false
            if (res != null) {
                analysis = res
                applyCleanCutout = !res.hasTransparentBackground
            } else {
                errorMessage = "Could not decode selected image. Please choose a valid PNG or image file."
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> handleSelectedUri(uri) }
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> handleSelectedUri(uri) }
    )

    val activeBitmap = remember(analysis, applyCleanCutout) {
        analysis?.let { if (applyCleanCutout) it.autoMasked512Bitmap else it.normalized512Bitmap }
    }

    val displayBitmap = remember(activeBitmap, showGuideOverlay) {
        val base = activeBitmap ?: return@remember null
        if (showGuideOverlay) {
            DlsUvTextureExporter.overlayDls26GuideLines(context, base)
        } else {
            base
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DLS 26 KIT LINK GENERATOR",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Upload official 512×512 PNG & generate game URL",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("uploader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step 1: Select Kit File
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "1. Upload Official DLS 26 Kit Texture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pick any 512×512 PNG kit template from your Photos or Files/Downloads. We verify the DLS 26 UV layout and host a permanent direct HTTPS .png link.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("upload_pick_gallery_button")
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("From Photos")
                                }
                                OutlinedButton(
                                    onClick = {
                                        filePicker.launch(arrayOf("image/png", "image/*"))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("upload_pick_file_button")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Browse PNG")
                                }
                            }
                        }
                    }
                }

                // Step 2: Interactive Checkerboard Preview & Diagnostics
                if (isAnalyzing) {
                    item {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Analyzing DLS 26 UV format...")
                            }
                        }
                    }
                } else if (analysis != null && displayBitmap != null) {
                    val info = analysis!!
                    item {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(
                                1.dp,
                                if (info.isOfficialDls26Aligned) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "2. 512×512 DLS UV Preview",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilterChip(
                                        selected = showGuideOverlay,
                                        onClick = { showGuideOverlay = !showGuideOverlay },
                                        label = { Text("UV Guides") },
                                        leadingIcon = {
                                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }

                                // Transparent alpha checkerboard canvas behind 512x512 PNG
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AlphaCheckerboardBackground(modifier = Modifier.fillMaxSize())
                                    Image(
                                        bitmap = displayBitmap.asImageBitmap(),
                                        contentDescription = "Uploaded DLS 26 kit UV sheet preview",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Verification status banner
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (info.isOfficialDls26Aligned) {
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = if (info.isOfficialDls26Aligned) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (info.isOfficialDls26Aligned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = info.statusTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = info.statusDetail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Optional Clean DLS 26 UV Mask Cutout Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Clean DLS 26 UV Cutout",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Strips background outside official DLS 26 jersey islands",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Switch(
                                        checked = applyCleanCutout,
                                        onCheckedChange = { applyCleanCutout = it }
                                    )
                                }
                            }
                        }
                    }

                    // Step 3: Name & Publish Live Link
                    item {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "3. Generate DLS 26 Game URL",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = kitTitleInput,
                                    onValueChange = { kitTitleInput = it },
                                    label = { Text("Kit Name / File Label") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("uploader_kit_name_input")
                                )

                                Button(
                                    enabled = !isUploading && activeBitmap != null,
                                    onClick = {
                                        val bmpToPublish = activeBitmap ?: return@Button
                                        isUploading = true
                                        errorMessage = null
                                        scope.launch {
                                            val result = DlsKitUrlPublisher.publishPngForDls26(
                                                bitmap = bmpToPublish,
                                                fileNameBase = kitTitleInput
                                            )
                                            isUploading = false
                                            if (result.success && result.url.isNotBlank()) {
                                                generatedUrl = result.url
                                                generatedHost = result.hostName
                                                clipboard.setText(AnnotatedString(result.url))
                                                sessionLinks.add(
                                                    0,
                                                    GeneratedUploadLinkEntry(
                                                        title = kitTitleInput.ifBlank { "Custom DLS 26 Kit" },
                                                        url = result.url,
                                                        hostName = result.hostName
                                                    )
                                                )
                                                snackbarHostState.showSnackbar("DLS 26 link generated & copied!")
                                            } else {
                                                errorMessage = result.errorMessage ?: "Upload failed"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("uploader_publish_button")
                                ) {
                                    if (isUploading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Publishing 512×512 Kit PNG...")
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Upload & Generate DLS 26 Link", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!errorMessage.isNullOrBlank()) {
                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                if (!generatedUrl.isNullOrBlank()) {
                                    val link = generatedUrl!!
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = "Ready for DLS 26 • ${generatedHost ?: "Direct PNG"}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = link,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        clipboard.setText(AnnotatedString(link))
                                                        scope.launch { snackbarHostState.showSnackbar("Copied DLS 26 Kit URL!") }
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("uploader_copy_url_button")
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Copy URL")
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(Intent.EXTRA_SUBJECT, kitTitleInput)
                                                            putExtra(Intent.EXTRA_TEXT, "$kitTitleInput (DLS 26 Kit URL):\n$link")
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "Share DLS 26 Kit URL"))
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Share Link")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty state + How to use guide
                    item {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "How Custom DLS 26 Kit Links Work",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "1. Pick your 512×512 PNG kit image using 'From Photos' or 'Browse PNG'.\n" +
                                        "2. Verify Collar, Torso, Sleeves, Shorts & Socks align inside the official DLS 26 UV guides.\n" +
                                        "3. Tap 'Upload & Generate DLS 26 Link' to host a direct HTTPS .png URL.\n" +
                                        "4. In Dream League Soccer 26: My Club → Customise → Kit → Custom Kit → Paste URL → Confirm.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Generated Links History (for multi-kit packs: Home, Away, Third, GK)
                if (sessionLinks.isNotEmpty()) {
                    item {
                        Text(
                            text = "GENERATED KIT LINKS (${sessionLinks.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(sessionLinks) { entry ->
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = entry.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(entry.url))
                                        scope.launch { snackbarHostState.showSnackbar("Copied ${entry.title} URL") }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlphaCheckerboardBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFF0F172A))) {
        val cell = 24f
        val cols = (size.width / cell).toInt() + 1
        val rows = (size.height / cell).toInt() + 1
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if ((r + c) % 2 == 0) {
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(c * cell, r * cell),
                        size = Size(cell, cell)
                    )
                }
            }
        }
    }
}
