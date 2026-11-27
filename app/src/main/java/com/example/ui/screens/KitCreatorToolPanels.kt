package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CrestPlacement
import com.example.data.model.JerseyPattern
import com.example.data.model.JerseyTemplateFit
import com.example.data.model.JerseyViewAngle
import com.example.data.model.KitSlotType
import com.example.data.model.ManufacturerMark
import com.example.data.model.ShoulderPattern
import com.example.data.model.SidePattern
import com.example.data.model.hexToHsl
import com.example.data.model.parseColorHex
import com.example.data.model.toHexString
import com.example.rendering.ClubCrestBadge
import com.example.rendering.ClubCrestStyle
import com.example.rendering.JerseyPreviewCanvas
import com.example.rendering.ProCrestRenderer
import com.example.rendering.computeReadableTextColor
import com.example.ui.components.ColorPaletteRow
import com.example.ui.viewmodel.KitCreatorUiState
import com.example.ui.viewmodel.KitCreatorViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitBasePanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = state.spec.clubName,
            onValueChange = viewModel::setClubName,
            label = { Text("Club Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kit_club_name_input")
        )

        Text("Jersey Base Slot", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KitSlotType.entries.forEach { slot ->
                FilterChip(
                    selected = state.spec.slotType == slot,
                    onClick = { viewModel.setSlotType(slot) },
                    label = { Text(slot.label) },
                    modifier = Modifier.testTag("base_slot_${slot.shortLabel}")
                )
            }
        }

        Text("Jersey Template Cut", style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            JerseyTemplateFit.entries.forEach { fit ->
                FilterChip(
                    selected = state.spec.templateFit == fit,
                    onClick = { viewModel.setTemplateFit(fit) },
                    label = { Text(fit.label) }
                )
            }
        }
    }
}

@Composable
fun KitColorsPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ColorPaletteRow(
            label = "Primary Jersey Color",
            selectedHex = state.spec.primaryColorHex,
            onColorSelected = viewModel::setPrimaryColor
        )
        ColorPaletteRow(
            label = "Secondary Pattern Color",
            selectedHex = state.spec.secondaryColorHex,
            onColorSelected = viewModel::setSecondaryColor
        )
        ColorPaletteRow(
            label = "Third / Accent Trim Color",
            selectedHex = state.spec.thirdColorHex,
            onColorSelected = viewModel::setThirdColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Vertical Base Gradient", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Blend primary into a secondary shade",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.spec.useGradient,
                onCheckedChange = viewModel::setUseGradient
            )
        }
        if (state.spec.useGradient) {
            ColorPaletteRow(
                label = "Gradient Bottom Color",
                selectedHex = state.spec.gradientEndHex,
                onColorSelected = viewModel::setGradientEnd
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Shorts & Socks Colors (Auto-Assembled in DLS 26 UV)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.spec.shortsColorHex.equals(state.spec.primaryColorHex, true) &&
                            state.spec.socksColorHex.equals(state.spec.primaryColorHex, true),
                        onClick = {
                            viewModel.applyShortsAndSocksPreset(
                                state.spec.primaryColorHex,
                                state.spec.primaryColorHex
                            )
                        },
                        label = { Text("Match Shirt") }
                    )
                    FilterChip(
                        selected = state.spec.shortsColorHex.equals("#FFFFFF", true),
                        onClick = {
                            viewModel.applyShortsAndSocksPreset("#FFFFFF", state.spec.primaryColorHex)
                        },
                        label = { Text("White Shorts") }
                    )
                    FilterChip(
                        selected = state.spec.shortsColorHex.equals("#111827", true) &&
                            state.spec.socksColorHex.equals("#111827", true),
                        onClick = {
                            viewModel.applyShortsAndSocksPreset("#111827", "#111827")
                        },
                        label = { Text("Obsidian Set") }
                    )
                }

                ColorPaletteRow(
                    label = "Shorts Color",
                    selectedHex = state.spec.shortsColorHex,
                    onColorSelected = viewModel::setShortsColor,
                    modifier = Modifier.testTag("shorts_color_row")
                )
                ColorPaletteRow(
                    label = "Shorts Side Trim",
                    selectedHex = state.spec.shortsTrimColorHex ?: state.spec.secondaryColorHex,
                    onColorSelected = viewModel::setShortsTrimColor,
                    modifier = Modifier.testTag("shorts_trim_color_row")
                )
                ColorPaletteRow(
                    label = "Socks Color",
                    selectedHex = state.spec.socksColorHex,
                    onColorSelected = viewModel::setSocksColor,
                    modifier = Modifier.testTag("socks_color_row")
                )
                ColorPaletteRow(
                    label = "Socks Cuff Stripe",
                    selectedHex = state.spec.socksTrimColorHex ?: state.spec.secondaryColorHex,
                    onColorSelected = viewModel::setSocksTrimColor,
                    modifier = Modifier.testTag("socks_trim_color_row")
                )
            }
        }
    }
}

@Composable
fun KitPatternPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Visual Kit Patterns (${JerseyPattern.entries.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scroll to preview patterns live on your kit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = state.spec.pattern.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("visual_pattern_grid"),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(JerseyPattern.entries, key = { it.name }) { pattern ->
                val isSelected = state.spec.pattern == pattern
                val previewSpec = state.spec.copy(
                    pattern = pattern,
                    playerName = "",
                    playerNumber = "",
                    customText = "",
                    sponsorText = ""
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pattern_${pattern.name}")
                        .clickable { viewModel.setPattern(pattern) }
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            JerseyPreviewCanvas(
                                spec = previewSpec,
                                isBackView = false,
                                showShortsPreview = false,
                                modifier = Modifier.height(84.dp)
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected pattern",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = pattern.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitDetailsPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "DLS 26 uses standard crew-neck short sleeves. Customize collar, sleeve & cuff colors, shoulder stripes, and side panels below — swipe the kit to Left/Right side to inspect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
            )
        }

        ColorPaletteRow(
            label = "Collar Color",
            selectedHex = state.spec.collarColorHex,
            onColorSelected = viewModel::setCollarColor
        )
        ColorPaletteRow(
            label = "Sleeve Color",
            selectedHex = state.spec.sleeveColorHex,
            onColorSelected = viewModel::setSleeveColor
        )
        ColorPaletteRow(
            label = "Sleeve Cuff Color",
            selectedHex = state.spec.cuffColorHex,
            onColorSelected = viewModel::setCuffColor
        )

        Text("Shoulder Pattern", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShoulderPattern.entries.forEach { sp ->
                FilterChip(
                    selected = state.spec.shoulderPattern == sp,
                    onClick = {
                        viewModel.setShoulderPattern(sp)
                        if (sp != ShoulderPattern.NONE && !state.showUvPreview && state.rotationAngleDeg !in 45f..135f && state.rotationAngleDeg !in 225f..315f) {
                            viewModel.snapToViewAngle(JerseyViewAngle.RIGHT_SIDE)
                        }
                    },
                    label = { Text(sp.label) }
                )
            }
        }

        Text("Side Panels", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SidePattern.entries.forEach { side ->
                FilterChip(
                    selected = state.spec.sidePattern == side,
                    onClick = {
                        viewModel.setSidePattern(side)
                        if (side != SidePattern.NONE && !state.showUvPreview && state.rotationAngleDeg !in 45f..135f && state.rotationAngleDeg !in 225f..315f) {
                            viewModel.snapToViewAngle(JerseyViewAngle.RIGHT_SIDE)
                        }
                    },
                    label = { Text(side.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitBrandingPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel,
    onPickClubLogo: () -> Unit,
    onPickSponsorLogo: () -> Unit
) {
    val autoContrastHex = computeReadableTextColor(
        parseColorHex(state.spec.primaryColorHex),
        parseColorHex(state.spec.secondaryColorHex),
        parseColorHex(state.spec.thirdColorHex)
    ).toHexString()

    val resolvedCrestStyle = ProCrestRenderer.resolveCrestStyle(
        clubName = state.spec.clubName,
        explicitStyleId = state.spec.crestStyleId
    )
    val crestPrimaryHex = state.spec.crestPrimaryColorHex ?: state.spec.primaryColorHex
    val crestSecondaryHex = state.spec.crestSecondaryColorHex ?: state.spec.secondaryColorHex
    val crestAccentHex = state.spec.crestAccentColorHex ?: state.spec.thirdColorHex
    val effectiveBrandHex = state.spec.manufacturerColorHex ?: autoContrastHex
    val effectiveSponsorHex = state.spec.sponsorColorHex ?: autoContrastHex
    val effectiveNumberHex = state.spec.playerNumberColorHex ?: autoContrastHex
    val autoShortsContrastHex = computeReadableTextColor(
        parseColorHex(state.spec.shortsColorHex),
        parseColorHex(state.spec.shortsTrimColorHex ?: state.spec.secondaryColorHex),
        parseColorHex(state.spec.thirdColorHex)
    ).toHexString()
    val effectiveShortsNumberHex = state.spec.shortsNumberColorHex
        ?: state.spec.playerNumberColorHex
        ?: autoShortsContrastHex
    val (numH, numS, numL) = hexToHsl(effectiveNumberHex)
    val (sNumH, sNumS, sNumL) = hexToHsl(effectiveShortsNumberHex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Club Crest Logo Picker (24 Pro Club Crests) & Crest Colors
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Club Crest Logos (${ClubCrestStyle.entries.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose any crest icon & customize its 3 colors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = resolvedCrestStyle.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("crest_picker_row")
                ) {
                    lazyRowItems(ClubCrestStyle.entries, key = { it.name }) { style ->
                        val isSelected = resolvedCrestStyle == style && state.spec.customClubLogoUri == null
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .width(84.dp)
                                .testTag("crest_style_${style.name}")
                                .clickable {
                                    viewModel.setCustomClubLogoUri(null)
                                    viewModel.setCrestStyleId(style.name)
                                    if (state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.FRONT)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ClubCrestBadge(
                                        primaryHex = crestPrimaryHex,
                                        secondaryHex = crestSecondaryHex,
                                        accentHex = crestAccentHex,
                                        shortCode = ProCrestRenderer.deriveShortMonogram(state.spec.clubName),
                                        clubName = state.spec.clubName,
                                        crestStyleId = style.name,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected crest",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Text("Crest Placement", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CrestPlacement.entries.forEach { placement ->
                        FilterChip(
                            selected = state.spec.crestPlacement == placement,
                            onClick = { viewModel.setCrestPlacement(placement) },
                            label = { Text(placement.label) }
                        )
                    }
                }

                ColorPaletteRow(
                    label = "Crest Base Color",
                    selectedHex = crestPrimaryHex,
                    onColorSelected = { viewModel.setCrestPrimaryColor(it) },
                    autoOptionLabel = "Match Kit",
                    isAutoSelected = state.spec.crestPrimaryColorHex == null,
                    onSelectAuto = { viewModel.setCrestPrimaryColor(null) },
                    modifier = Modifier.testTag("crest_primary_color_row")
                )
                ColorPaletteRow(
                    label = "Crest Detail Color",
                    selectedHex = crestSecondaryHex,
                    onColorSelected = { viewModel.setCrestSecondaryColor(it) },
                    autoOptionLabel = "Match Kit",
                    isAutoSelected = state.spec.crestSecondaryColorHex == null,
                    onSelectAuto = { viewModel.setCrestSecondaryColor(null) },
                    modifier = Modifier.testTag("crest_secondary_color_row")
                )
                ColorPaletteRow(
                    label = "Crest Border & Stars Color",
                    selectedHex = crestAccentHex,
                    onColorSelected = { viewModel.setCrestAccentColor(it) },
                    autoOptionLabel = "Match Kit",
                    isAutoSelected = state.spec.crestAccentColorHex == null,
                    onSelectAuto = { viewModel.setCrestAccentColor(null) },
                    modifier = Modifier.testTag("crest_accent_color_row")
                )
            }
        }

        // 2. Manufacturer Mark & Custom Color
        Text("Manufacturer Mark", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ManufacturerMark.entries.forEach { m ->
                FilterChip(
                    selected = state.spec.manufacturer == m,
                    onClick = { viewModel.setManufacturer(m) },
                    label = { Text(m.label) }
                )
            }
        }
        ColorPaletteRow(
            label = "Manufacturer Mark Color",
            selectedHex = effectiveBrandHex,
            onColorSelected = { viewModel.setManufacturerColor(it) },
            autoOptionLabel = "Auto",
            isAutoSelected = state.spec.manufacturerColorHex == null,
            onSelectAuto = { viewModel.setManufacturerColor(null) },
            modifier = Modifier.testTag("manufacturer_color_row")
        )

        // 3. Custom PNG Uploads (Crest & Sponsor)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPickClubLogo,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (state.spec.customClubLogoUri != null) "Change Crest" else "Upload Crest")
            }
            OutlinedButton(
                onClick = onPickSponsorLogo,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (state.spec.customSponsorLogoUri != null) "Change Sponsor" else "Upload Sponsor")
            }
        }
        if (state.spec.customClubLogoUri != null || state.spec.customSponsorLogoUri != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.spec.customClubLogoUri != null) {
                    TextButton(onClick = { viewModel.setCustomClubLogoUri(null) }) {
                        Text("Clear Custom Crest")
                    }
                }
                if (state.spec.customSponsorLogoUri != null) {
                    TextButton(onClick = { viewModel.setCustomSponsorLogoUri(null) }) {
                        Text("Clear Sponsor Image")
                    }
                }
            }
        }

        // 4. Sponsor Wordmark & Color
        OutlinedTextField(
            value = state.spec.sponsorText,
            onValueChange = viewModel::setSponsorText,
            label = { Text("Sponsor Wordmark") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        ColorPaletteRow(
            label = "Sponsor Wordmark Color",
            selectedHex = effectiveSponsorHex,
            onColorSelected = { viewModel.setSponsorColor(it) },
            autoOptionLabel = "Auto",
            isAutoSelected = state.spec.sponsorColorHex == null,
            onSelectAuto = { viewModel.setSponsorColor(null) },
            modifier = Modifier.testTag("sponsor_color_row")
        )

        OutlinedTextField(
            value = state.spec.customText,
            onValueChange = viewModel::setCustomText,
            label = { Text("Custom Chest Motto (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Back Player Name, Number & DLS 26 Number Color Matching
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Player Name & Number (DLS 26 Match)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.spec.playerName,
                        onValueChange = {
                            viewModel.setPlayerName(it)
                            if (!state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.BACK)
                        },
                        label = { Text("Player Name (Back)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("player_name_input")
                    )
                    OutlinedTextField(
                        value = state.spec.playerNumber,
                        onValueChange = {
                            viewModel.setPlayerNumber(it)
                            if (!state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.BACK)
                        },
                        label = { Text("Number") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_number_input")
                    )
                }

                ColorPaletteRow(
                    label = "Jersey Number & Name Color",
                    selectedHex = effectiveNumberHex,
                    onColorSelected = {
                        viewModel.setPlayerNumberColor(it)
                        if (!state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.BACK)
                    },
                    autoOptionLabel = "Auto",
                    isAutoSelected = state.spec.playerNumberColorHex == null,
                    onSelectAuto = { viewModel.setPlayerNumberColor(null) },
                    modifier = Modifier.testTag("player_number_color_row")
                )

                ColorPaletteRow(
                    label = "Shorts Number Color",
                    selectedHex = effectiveShortsNumberHex,
                    onColorSelected = {
                        viewModel.setShortsNumberColor(it)
                        viewModel.toggleShortsPreview(true)
                        if (state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.FRONT)
                    },
                    autoOptionLabel = "Match Jersey",
                    isAutoSelected = state.spec.shortsNumberColorHex == null,
                    onSelectAuto = { viewModel.setShortsNumberColor(null) },
                    modifier = Modifier.testTag("shorts_number_color_row")
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(parseColorHex(effectiveNumberHex))
                            )
                            Text(
                                text = "Jersey # UV Square: H ${numH.roundToInt()}° • S ${(numS * 100f).roundToInt()}% • L ${(numL * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(parseColorHex(effectiveShortsNumberHex))
                            )
                            Text(
                                text = "Shorts # UV Square: H ${sNumH.roundToInt()}° • S ${(sNumS * 100f).roundToInt()}% • L ${(sNumL * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clean Back Number Box", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Adds solid contrast panel behind back number on striped kits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.spec.cleanBackNumberBox,
                        onCheckedChange = {
                            viewModel.setCleanBackNumberBox(it)
                            if (!state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.BACK)
                        },
                        modifier = Modifier.testTag("clean_back_box_switch")
                    )
                }
            }
        }
    }
}

@Composable
fun KitExportPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel,
    isGeneratingUrl: Boolean,
    generatedKitUrl: String?,
    generatedUrlHost: String?,
    urlErrorMessage: String?,
    onSave: () -> Unit,
    onDuplicate: () -> Unit,
    onExportDlsUv512: () -> Unit,
    onExportPreviewPng: () -> Unit,
    onShareKit: () -> Unit,
    onGenerateLiveDlsUrl: () -> Unit,
    onCopyLiveDlsUrl: (String) -> Unit,
    onOpenKitUploader: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.designName,
            onValueChange = viewModel::setDesignName,
            label = { Text("Design Title") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("design_title_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_kit_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save Kit", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onDuplicate,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FileCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save as Copy")
            }
        }

        Button(
            onClick = onExportDlsUv512,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("export_dls_uv_button")
        ) {
            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Export 512×512 DLS 26 UV Kit (PNG)")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onExportPreviewPng,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Preview PNG")
            }
            OutlinedButton(
                onClick = onShareKit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share")
            }
        }

        OutlinedButton(
            enabled = !isGeneratingUrl,
            onClick = onGenerateLiveDlsUrl,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_live_kit_url_button")
        ) {
            if (isGeneratingUrl) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Publishing 512×512 Kit to DLS 26 URL...")
            } else {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (generatedKitUrl == null) "Generate Live DLS 26 Kit URL (.png)"
                    else "Regenerate Live DLS 26 URL"
                )
            }
        }

        if (!urlErrorMessage.isNullOrBlank()) {
            Text(
                text = urlErrorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!generatedKitUrl.isNullOrBlank()) {
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
                        text = "Live DLS 26 Kit URL${generatedUrlHost?.let { " • $it" } ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = generatedKitUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { onCopyLiveDlsUrl(generatedKitUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_generated_kit_url_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy DLS 26 URL")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onOpenKitUploader,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("creator_open_uploader_button")
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Upload Official 512×512 Kit & Get Link")
        }
    }
}
