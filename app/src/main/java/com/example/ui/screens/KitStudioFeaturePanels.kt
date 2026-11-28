package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChestBadgeStyle
import com.example.data.model.JerseyPattern
import com.example.data.model.JerseyViewAngle
import com.example.data.model.KitSlotType
import com.example.data.model.SleevePatchStyle
import com.example.data.model.SponsorStyle
import com.example.data.model.parseColorHex
import com.example.data.remote.GoalkeeperStudioCatalog
import com.example.rendering.JerseyPreviewCanvas
import com.example.rendering.KeeperGlovesPreview
import com.example.ui.components.ColorPaletteRow
import com.example.ui.viewmodel.KitCreatorUiState
import com.example.ui.viewmodel.KitCreatorViewModel
import kotlin.math.roundToInt

/**
 * Dedicated GK Studio tool panel:
 *  - Matching Keeper Gloves Preview & Glove Accent Picker
 *  - 4 Goalkeeper-specific geometric/splatter patterns
 *  - 7 One-tap Neon Keeper Palettes
 *  - 8 Pro Goalkeeper Templates
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitGkStudioPanel(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel
) {
    val gkPatterns = JerseyPattern.entries.filter { it.isGoalkeeper }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gk_studio_panel"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Matching Keeper Gloves Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(114.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                        .testTag("gk_gloves_preview_card"),
                    contentAlignment = Alignment.Center
                ) {
                    KeeperGlovesPreview(
                        spec = state.spec,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsHandball,
                            contentDescription = "Keeper gloves",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Matching GK Gloves",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Backhand + latex grip palm auto-sync with your goalkeeper jersey colors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.spec.slotType != KitSlotType.GOALKEEPER) {
                        OutlinedButton(
                            onClick = { viewModel.setSlotType(KitSlotType.GOALKEEPER) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Switch Slot to GK", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        ColorPaletteRow(
            label = "Keeper Gloves Color",
            selectedHex = state.spec.gkGloveColorHex ?: state.spec.primaryColorHex,
            onColorSelected = { viewModel.setGkGloveColor(it) },
            autoOptionLabel = "Match Kit",
            isAutoSelected = state.spec.gkGloveColorHex == null,
            onSelectAuto = { viewModel.setGkGloveColor(null) }
        )

        // GK Pattern Pack
        Text(
            text = "Goalkeeper Pattern Pack (${gkPatterns.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            gkPatterns.forEach { pat ->
                FilterChip(
                    selected = state.spec.pattern == pat,
                    onClick = {
                        viewModel.setSlotType(KitSlotType.GOALKEEPER)
                        viewModel.setPattern(pat)
                    },
                    label = { Text(pat.label) },
                    modifier = Modifier.testTag("gk_pattern_${pat.name}")
                )
            }
        }

        // Neon Keeper Palettes
        Text(
            text = "Neon Keeper Palettes (${GoalkeeperStudioCatalog.neonPalettes.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gk_neon_palettes_row")
        ) {
            items(GoalkeeperStudioCatalog.neonPalettes, key = { it.id }) { palette ->
                val isActive = state.spec.primaryColorHex.equals(palette.primaryHex, true) &&
                    state.spec.secondaryColorHex.equals(palette.secondaryHex, true)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .width(102.dp)
                        .testTag("gk_palette_${palette.id}")
                        .clickable { viewModel.applyGkNeonPalette(palette) }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(palette.primaryHex))
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(palette.secondaryHex))
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseColorHex(palette.thirdHex))
                            )
                        }
                        Text(
                            text = palette.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Pro Goalkeeper Templates
        Text(
            text = "GK Studio Templates (${GoalkeeperStudioCatalog.gkTemplates.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gk_templates_row")
        ) {
            items(GoalkeeperStudioCatalog.gkTemplates, key = { it.id }) { tpl ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .width(136.dp)
                        .testTag("gk_template_${tpl.id}")
                        .clickable { viewModel.applyGkTemplate(tpl) }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        JerseyPreviewCanvas(
                            spec = tpl.designSpec,
                            showShortsPreview = false,
                            modifier = Modifier.height(82.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = tpl.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = tpl.designSpec.pattern.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sponsor Style Gallery + Competition Sleeve Patches + World-Champion Chest Patch
 * + Captain Armband + Custom PNG Sponsor & Sticker Position/Scale Sliders.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KitSponsorAndBadgesSection(
    state: KitCreatorUiState,
    viewModel: KitCreatorViewModel,
    onPickCustomSticker: () -> Unit
) {
    val quickSponsors = listOf("APEX ENERGY", "FLY SKYLINE", "QATAR AIR", "NOVA TECH", "SONIC+", "CROWN BANK")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Vector Sponsor Style Gallery
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sponsor_gallery_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Vector Sponsor Style Gallery",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SponsorStyle.entries.forEach { style ->
                        FilterChip(
                            selected = state.spec.sponsorStyle == style,
                            onClick = {
                                viewModel.setCustomSponsorLogoUri(null)
                                viewModel.setSponsorStyle(style)
                                if (state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.FRONT)
                            },
                            label = { Text(style.label) },
                            modifier = Modifier.testTag("sponsor_style_${style.name}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSponsors.forEach { preset ->
                        FilterChip(
                            selected = state.spec.sponsorText.equals(preset, true),
                            onClick = { viewModel.setSponsorText(preset) },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Position & Scale sliders for uploaded Sponsor PNG
                if (state.spec.customSponsorLogoUri != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sponsor_png_sliders")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sponsor PNG Placement & Scale", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                TextButton(
                                    onClick = {
                                        viewModel.setCustomSponsorOffsetX(0f)
                                        viewModel.setCustomSponsorOffsetY(0f)
                                        viewModel.setCustomSponsorScale(1f)
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            SliderRow("Horizontal X", state.spec.customSponsorOffsetX, -1f..1f, viewModel::setCustomSponsorOffsetX)
                            SliderRow("Vertical Y", state.spec.customSponsorOffsetY, -1f..1f, viewModel::setCustomSponsorOffsetY)
                            SliderRow("Scale", state.spec.customSponsorScale, 0.4f..1.8f, viewModel::setCustomSponsorScale)
                        }
                    }
                }
            }
        }

        // Competition Sleeve Patches, World-Champion Chest Patch & Captain Armband
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("badges_and_patches_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Competition Patches & Captain Armband",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("World-Champion / Winner Chest Patch", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChestBadgeStyle.entries.forEach { badge ->
                        FilterChip(
                            selected = state.spec.chestBadge == badge,
                            onClick = {
                                viewModel.setChestBadge(badge)
                                if (state.isBackView) viewModel.snapToViewAngle(JerseyViewAngle.FRONT)
                            },
                            label = { Text(badge.label) },
                            modifier = Modifier.testTag("chest_badge_${badge.name}")
                        )
                    }
                }

                Text("Right Sleeve Competition Patch", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SleevePatchStyle.entries.forEach { patch ->
                        FilterChip(
                            selected = state.spec.sleevePatch == patch,
                            onClick = { viewModel.setSleevePatch(patch) },
                            label = { Text(patch.label) },
                            modifier = Modifier.testTag("sleeve_patch_${patch.name}")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Captain Armband (Left Sleeve)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Adds pro 'C' armband to left sleeve preview & DLS 26 UV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.spec.captainArmbandEnabled,
                        onCheckedChange = viewModel::setCaptainArmbandEnabled,
                        modifier = Modifier.testTag("captain_armband_switch")
                    )
                }

                if (state.spec.captainArmbandEnabled) {
                    ColorPaletteRow(
                        label = "Armband Color",
                        selectedHex = state.spec.captainArmbandColorHex,
                        onColorSelected = viewModel::setCaptainArmbandColor
                    )
                }
            }
        }

        // Custom PNG Sticker Overlay + Position/Scale Sliders
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_sticker_overlay_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Custom PNG Sticker Overlay",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload any transparent PNG sticker/graphic and adjust its exact position & scale on the jersey.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickCustomSticker,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_custom_sticker_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.spec.customStickerUri != null) "Change PNG Sticker" else "Upload PNG Sticker")
                    }
                    if (state.spec.customStickerUri != null) {
                        TextButton(onClick = { viewModel.setCustomStickerUri(null) }) {
                            Text("Remove")
                        }
                    }
                }

                if (state.spec.customStickerUri != null) {
                    SliderRow("Sticker X", state.spec.customStickerOffsetX, -1f..1f, viewModel::setCustomStickerOffsetX)
                    SliderRow("Sticker Y", state.spec.customStickerOffsetY, -1f..1f, viewModel::setCustomStickerOffsetY)
                    SliderRow("Sticker Scale", state.spec.customStickerScale, 0.4f..1.8f, viewModel::setCustomStickerScale)
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(78.dp)
        )
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        )
        Text(
            text = "${(value * 100f).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(42.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Live 9:16 Matchday Poster & Squad Card Exporter Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchdayPosterPreviewSheet(
    posterBitmap: Bitmap,
    dlsKitUrl: String?,
    isPublishingUrl: Boolean,
    onDismiss: () -> Unit,
    onPublishUrlForQr: () -> Unit,
    onSavePoster: () -> Unit,
    onSharePoster: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 26.dp)
                .testTag("matchday_poster_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Matchday Poster & Squad Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1080×1920 Vertical • Instagram, TikTok & WhatsApp ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Image(
                bitmap = posterBitmap.asImageBitmap(),
                contentDescription = "Matchday Poster Preview",
                modifier = Modifier
                    .height(330.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (!dlsKitUrl.isNullOrBlank()) "Live DLS 26 URL Bound to QR" else "Design Code QR Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dlsKitUrl ?: "Publish live URL to make QR open your .png link directly",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (dlsKitUrl.isNullOrBlank()) {
                        TextButton(
                            enabled = !isPublishingUrl,
                            onClick = onPublishUrlForQr,
                            modifier = Modifier.testTag("poster_bind_url_button")
                        ) {
                            if (isPublishingUrl) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Bind URL")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSavePoster,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("poster_save_gallery_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save Poster", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onSharePoster,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("poster_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Story")
                }
            }
        }
    }
}
