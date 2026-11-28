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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.SponsorStyle
import com.example.data.model.parseColorHex
import com.example.rendering.ClubCrestBadge
import com.example.rendering.ClubCrestStyle
import com.example.rendering.DlsUvReverseEditConfig
import com.example.rendering.ExtractedDlsKitPalette
import com.example.ui.components.ColorPaletteRow
import kotlin.math.roundToInt

/**
 * Direct DLS 26 Kit Importer Palette Extractor + Reverse Editor Panel:
 * Displays auto-extracted jersey/shorts/socks/number swatches from any uploaded 512x512 PNG,
 * lets the user open the extracted palette directly inside Kit Studio, OR directly re-tint
 * islands and stamp custom club crests & sponsors right onto the uploaded 512x512 UV sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DlsKitReverseEditorPanel(
    palette: ExtractedDlsKitPalette,
    edits: DlsUvReverseEditConfig,
    onEditsChange: (DlsUvReverseEditConfig) -> Unit,
    onOpenPaletteInStudio: () -> Unit,
    onPickOverlayCrest: () -> Unit,
    onPickOverlaySponsor: () -> Unit,
    onSaveEditedPng: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dls_reverse_editor_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Extracted Palette & Reverse Editor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-sampled from official 512×512 DLS UV islands",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Auto-extracted 8-color swatch bar
            val swatches = listOf(
                "Jersey" to palette.jerseyPrimaryHex,
                "Accent" to palette.jerseySecondaryHex,
                "Sleeve" to palette.sleeveHex,
                "Collar" to palette.collarHex,
                "Shorts" to palette.shortsHex,
                "Socks" to palette.socksHex,
                "J#" to palette.jerseyNumberHex,
                "S#" to palette.shortsNumberHex
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("extracted_palette_row")
            ) {
                items(swatches) { (label, hex) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(hex))
                            )
                            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                hex,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onOpenPaletteInStudio,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("import_palette_to_studio_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Extracted Palette in Kit Creator Studio")
            }

            // Direct Re-tint Controls on Uploaded UV
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Direct UV Re-Tint & Overlays",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (edits.hasAnyActiveEdit) {
                            TextButton(
                                onClick = { onEditsChange(DlsUvReverseEditConfig()) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }
                    }

                    ColorPaletteRow(
                        label = "Re-Tint Jersey & Sleeves",
                        selectedHex = edits.retintJerseyHex ?: palette.jerseyPrimaryHex,
                        onColorSelected = { onEditsChange(edits.copy(retintJerseyHex = it)) },
                        autoOptionLabel = "Original",
                        isAutoSelected = edits.retintJerseyHex == null,
                        onSelectAuto = { onEditsChange(edits.copy(retintJerseyHex = null)) }
                    )
                    if (edits.retintJerseyHex != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tint Strength", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(84.dp))
                            Slider(
                                value = edits.retintJerseyStrength,
                                onValueChange = { onEditsChange(edits.copy(retintJerseyStrength = it)) },
                                valueRange = 0.15f..1f,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${(edits.retintJerseyStrength * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    ColorPaletteRow(
                        label = "Re-Tint Shorts",
                        selectedHex = edits.retintShortsHex ?: palette.shortsHex,
                        onColorSelected = { onEditsChange(edits.copy(retintShortsHex = it)) },
                        autoOptionLabel = "Original",
                        isAutoSelected = edits.retintShortsHex == null,
                        onSelectAuto = { onEditsChange(edits.copy(retintShortsHex = null)) }
                    )

                    ColorPaletteRow(
                        label = "Re-Tint Socks",
                        selectedHex = edits.retintSocksHex ?: palette.socksHex,
                        onColorSelected = { onEditsChange(edits.copy(retintSocksHex = it)) },
                        autoOptionLabel = "Original",
                        isAutoSelected = edits.retintSocksHex == null,
                        onSelectAuto = { onEditsChange(edits.copy(retintSocksHex = null)) }
                    )

                    // Overlay Custom Club Crest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Custom Club Crest", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Stamps crest onto Front Chest + Shorts UV",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = edits.overlayCrestEnabled,
                            onCheckedChange = { onEditsChange(edits.copy(overlayCrestEnabled = it)) },
                            modifier = Modifier.testTag("reverse_overlay_crest_switch")
                        )
                    }
                    if (edits.overlayCrestEnabled) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ClubCrestStyle.entries, key = { it.name }) { style ->
                                val selected = edits.overlayCrestStyleId == style.name && edits.overlayCustomCrestUri == null
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .width(68.dp)
                                        .clickable {
                                            onEditsChange(
                                                edits.copy(
                                                    overlayCrestStyleId = style.name,
                                                    overlayCustomCrestUri = null
                                                )
                                            )
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        ClubCrestBadge(
                                            primaryHex = edits.overlayCrestPrimaryHex,
                                            secondaryHex = edits.overlayCrestSecondaryHex,
                                            accentHex = edits.overlayCrestAccentHex,
                                            shortCode = "FC",
                                            crestStyleId = style.name,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Text(
                                            style.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onPickOverlayCrest, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (edits.overlayCustomCrestUri != null) "Change Crest PNG" else "Upload Crest PNG")
                            }
                            if (edits.overlayCustomCrestUri != null) {
                                TextButton(onClick = { onEditsChange(edits.copy(overlayCustomCrestUri = null)) }) {
                                    Text("Use Vector")
                                }
                            }
                        }
                    }

                    // Overlay Custom Sponsor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stamp Custom Sponsor", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Adds styled sponsor or PNG onto Front Torso UV",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = edits.overlaySponsorEnabled,
                            onCheckedChange = { onEditsChange(edits.copy(overlaySponsorEnabled = it)) },
                            modifier = Modifier.testTag("reverse_overlay_sponsor_switch")
                        )
                    }
                    if (edits.overlaySponsorEnabled) {
                        OutlinedTextField(
                            value = edits.overlaySponsorText,
                            onValueChange = { onEditsChange(edits.copy(overlaySponsorText = it)) },
                            label = { Text("Sponsor Text") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SponsorStyle.entries.forEach { st ->
                                FilterChip(
                                    selected = edits.overlaySponsorStyle == st,
                                    onClick = { onEditsChange(edits.copy(overlaySponsorStyle = st, overlayCustomSponsorUri = null)) },
                                    label = { Text(st.label) }
                                )
                            }
                        }
                        ColorPaletteRow(
                            label = "Sponsor Color",
                            selectedHex = edits.overlaySponsorColorHex,
                            onColorSelected = { onEditsChange(edits.copy(overlaySponsorColorHex = it)) }
                        )
                        OutlinedButton(onClick = onPickOverlaySponsor, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (edits.overlayCustomSponsorUri != null) "Change Sponsor PNG" else "Or Upload Sponsor PNG")
                        }
                    }

                    if (edits.hasAnyActiveEdit) {
                        OutlinedButton(
                            onClick = onSaveEditedPng,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reverse_save_edited_png_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Edited 512×512 PNG to Gallery")
                        }
                    }
                }
            }
        }
    }
}
