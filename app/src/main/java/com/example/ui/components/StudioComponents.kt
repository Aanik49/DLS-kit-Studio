package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.wrapContentWidth
import com.example.data.local.AppLanguage
import com.example.data.model.ClubKitBundle
import com.example.data.model.hexToHsl
import com.example.data.model.hslToHex
import com.example.data.model.parseColorHex
import com.example.data.model.toHexString
import com.example.rendering.ClubCrestBadge
import com.example.rendering.JerseyPreviewCanvas
import kotlin.math.roundToInt

data class StudioLocalizedCopy(
    val navHome: String,
    val navKits: String,
    val navCreate: String,
    val navLogos: String,
    val navProfile: String,
    val heroTitle: String,
    val heroSubtitle: String,
    val trendingTitle: String,
    val recentTitle: String,
    val popularClubsTitle: String,
    val nationalTeamsTitle: String,
    val templatesTitle: String,
    val myDesignsTitle: String
)

fun localizedCopy(language: AppLanguage): StudioLocalizedCopy {
    return when (language) {
        AppLanguage.ENGLISH -> StudioLocalizedCopy(
            navHome = "Home",
            navKits = "Kits",
            navCreate = "Create",
            navLogos = "Logos",
            navProfile = "Profile",
            heroTitle = "DESIGN YOUR DREAM CLUB",
            heroSubtitle = "Pro 512×512 DLS UV kits, custom crests & club packs.",
            trendingTitle = "FEATURED & TRENDING KITS",
            recentTitle = "RECENTLY ADDED",
            popularClubsTitle = "POPULAR CLUB COLLECTIONS",
            nationalTeamsTitle = "NATIONAL TEAM COLLECTIONS",
            templatesTitle = "FEATURED TEMPLATES",
            myDesignsTitle = "My Designs Library"
        )
        AppLanguage.SPANISH -> StudioLocalizedCopy(
            navHome = "Inicio",
            navKits = "Kits",
            navCreate = "Crear",
            navLogos = "Escudos",
            navProfile = "Perfil",
            heroTitle = "DISEÑA TU CLUB SOÑADO",
            heroSubtitle = "Kits DLS 512×512 UV, escudos personalizados y ligas.",
            trendingTitle = "KITS DESTACADOS",
            recentTitle = "RECIÉN AÑADIDOS",
            popularClubsTitle = "CLUBES POPULARES",
            nationalTeamsTitle = "SELECCIONES NACIONALES",
            templatesTitle = "PLANTILLAS DESTACADAS",
            myDesignsTitle = "Biblioteca de Diseños"
        )
        AppLanguage.PORTUGUESE -> StudioLocalizedCopy(
            navHome = "Início",
            navKits = "Kits",
            navCreate = "Criar",
            navLogos = "Escudos",
            navProfile = "Perfil",
            heroTitle = "CRIE O SEU CLUBE IDEAL",
            heroSubtitle = "Kits DLS 512×512 UV, emblemas e coleções de clubes.",
            trendingTitle = "KITS EM DESTAQUE",
            recentTitle = "ADICIONADOS RECENTEMENTE",
            popularClubsTitle = "CLUBES POPULARES",
            nationalTeamsTitle = "SELEÇÕES NACIONAIS",
            templatesTitle = "MODELOS EM DESTAQUE",
            myDesignsTitle = "Meus Projetos"
        )
        AppLanguage.FRENCH -> StudioLocalizedCopy(
            navHome = "Accueil",
            navKits = "Maillots",
            navCreate = "Créer",
            navLogos = "Logos",
            navProfile = "Profil",
            heroTitle = "CRÉEZ VOTRE CLUB DE RÊVE",
            heroSubtitle = "Kits DLS 512×512 UV, écussons et collections pro.",
            trendingTitle = "MAILLOTS TENDANCES",
            recentTitle = "AJOUTÉS RÉCEMMENT",
            popularClubsTitle = "CLUBS POPULAIRES",
            nationalTeamsTitle = "ÉQUIPES NATIONALES",
            templatesTitle = "MODÈLES EN VEDETTE",
            myDesignsTitle = "Mes Créations"
        )
    }
}

val ProFootballColorSwatches = listOf(
    "#FFFFFF", "#0B132B", "#111827", "#D70022", "#004D98",
    "#00E5FF", "#00E676", "#FFDF00", "#FFC400", "#6CABDD",
    "#A50044", "#F48FB1", "#FF6D00", "#7C3AED", "#018749",
    "#991B1B", "#1E3A8A", "#475569"
)

@Composable
fun StudioSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = actionLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun KitShowcaseCard(
    club: ClubKitBundle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "cardScale"
    )
    val homeSpec = club.variants.first().designSpec

    Card(
        modifier = modifier
            .scale(scale)
            .testTag("kit_card_${club.id}")
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                JerseyPreviewCanvas(
                    spec = homeSpec,
                    modifier = Modifier.height(134.dp)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = club.season,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                ClubCrestBadge(
                    primaryHex = club.crestPrimaryHex,
                    secondaryHex = club.crestSecondaryHex,
                    shortCode = club.shortCode,
                    clubName = club.clubName,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(30.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = club.clubName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = club.league,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${club.downloads / 1000}k",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                club.variants.forEach { variant ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(parseColorHex(variant.designSpec.primaryColorHex))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPaletteRow(
    label: String,
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoOptionLabel: String? = null,
    isAutoSelected: Boolean = false,
    onSelectAuto: (() -> Unit)? = null
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isAutoSelected && autoOptionLabel != null) {
                    "$autoOptionLabel (${selectedHex.uppercase()})"
                } else {
                    selectedHex.uppercase()
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { showCustomDialog = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Colorize,
                            contentDescription = "Custom HSL color picker",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (autoOptionLabel != null && onSelectAuto != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .height(38.dp)
                            .wrapContentWidth()
                            .clickable { onSelectAuto() },
                        shape = RoundedCornerShape(19.dp),
                        color = if (isAutoSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            width = if (isAutoSelected) 2.dp else 1.dp,
                            color = if (isAutoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = autoOptionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isAutoSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAutoSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
            items(ProFootballColorSwatches) { hex ->
                val isSelected = !isAutoSelected && hex.equals(selectedHex, ignoreCase = true)
                val color = parseColorHex(hex)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        val lum = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (lum > 0.6f) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomRgbColorDialog(
            initialHex = selectedHex,
            onDismiss = { showCustomDialog = false },
            onConfirm = { newHex ->
                onColorSelected(newHex)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomRgbColorDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialHsl = remember(initialHex) { hexToHsl(initialHex) }
    var hue by remember { mutableFloatStateOf(initialHsl.first) }
    var saturation by remember { mutableFloatStateOf(initialHsl.second) }
    var lightness by remember { mutableFloatStateOf(initialHsl.third) }
    var hexText by remember { mutableStateOf(hslToHex(initialHsl.first, initialHsl.second, initialHsl.third)) }

    val currentHex = hslToHex(hue, saturation, lightness)
    val previewColor = parseColorHex(currentHex)
    val previewLum = 0.299f * previewColor.red + 0.587f * previewColor.green + 0.114f * previewColor.blue
    val previewInk = if (previewLum > 0.58f) Color(0xFF0B132B) else Color.White

    val hueGradient = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFFF0000),
                Color(0xFFFFFF00),
                Color(0xFF00FF00),
                Color(0xFF00FFFF),
                Color(0xFF0000FF),
                Color(0xFFFF00FF),
                Color(0xFFFF0000)
            )
        )
    }
    val satGradient = Brush.horizontalGradient(
        listOf(
            parseColorHex(hslToHex(hue, 0f, lightness)),
            parseColorHex(hslToHex(hue, 1f, lightness))
        )
    )
    val lightGradient = Brush.horizontalGradient(
        listOf(
            Color.Black,
            parseColorHex(hslToHex(hue, saturation, 0.5f)),
            Color.White
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("DLS 26 HSL Color Picker", style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(previewColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentHex.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = previewInk
                        )
                        Text(
                            text = "H ${hue.roundToInt()}° • S ${(saturation * 100f).roundToInt()}% • L ${(lightness * 100f).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = previewInk.copy(alpha = 0.85f)
                        )
                    }
                }

                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        val cleaned = input.uppercase().take(7)
                        hexText = cleaned
                        val normalized = if (cleaned.startsWith("#")) cleaned else "#$cleaned"
                        if (normalized.matches(Regex("^#[0-9A-F]{6}$"))) {
                            val (nh, ns, nl) = hexToHsl(normalized)
                            hue = nh
                            saturation = ns
                            lightness = nl
                        }
                    },
                    label = { Text("Exact sRGB Hex (#RRGGBB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // COLOUR (Hue 0..360) matching DLS 26
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("COLOUR (Hue)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${hue.roundToInt()}°", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(hueGradient)
                    )
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            hexText = hslToHex(it, saturation, lightness)
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = previewColor,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                // SATURATION (0..100%) matching DLS 26
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SATURATION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${(saturation * 100f).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(satGradient)
                    )
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            hexText = hslToHex(hue, it, lightness)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = previewColor,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                // LIGHTNESS (0..100%) matching DLS 26
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LIGHTNESS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${(lightness * 100f).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(lightGradient)
                    )
                    Slider(
                        value = lightness,
                        onValueChange = {
                            lightness = it
                            hexText = hslToHex(hue, saturation, it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = previewColor,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                // Quick Pro Presets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProFootballColorSwatches.take(12)) { preset ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(parseColorHex(preset))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    val (ph, ps, pl) = hexToHsl(preset)
                                    hue = ph
                                    saturation = ps
                                    lightness = pl
                                    hexText = preset.uppercase()
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentHex) }) {
                Text("Apply Color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ShimmerSkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 250f, 0f),
        end = Offset(translateAnim, 250f)
    )
    Box(
        modifier = modifier
            .height(190.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(brush)
    )
}
