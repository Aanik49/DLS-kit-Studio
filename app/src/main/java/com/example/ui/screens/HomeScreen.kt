package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RecentlyViewedEntity
import com.example.data.model.ClubKitBundle
import com.example.data.model.CustomizationTemplate
import com.example.rendering.JerseyPreviewCanvas
import com.example.ui.components.KitShowcaseCard
import com.example.ui.components.StudioLocalizedCopy
import com.example.ui.components.StudioSectionHeader
import com.example.ui.viewmodel.CatalogSpecialFilter

@Composable
fun HomeScreen(
    copy: StudioLocalizedCopy,
    trendingClubs: List<ClubKitBundle>,
    recentlyAddedClubs: List<ClubKitBundle>,
    popularClubCollections: List<ClubKitBundle>,
    nationalTeams: List<ClubKitBundle>,
    featuredTemplates: List<CustomizationTemplate>,
    gkTemplates: List<CustomizationTemplate> = emptyList(),
    recentlyViewed: List<RecentlyViewedEntity>,
    allClubs: List<ClubKitBundle>,
    onSearchClicked: () -> Unit,
    onClubSelected: (ClubKitBundle) -> Unit,
    onSeeAllFilter: (CatalogSpecialFilter) -> Unit,
    onOpenKitCreator: () -> Unit,
    onOpenGkStudio: () -> Unit = onOpenKitCreator,
    onOpenLogoCreator: () -> Unit,
    onOpenKitUploader: () -> Unit,
    onUseTemplate: (CustomizationTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentlyViewedBundles = recentlyViewed.mapNotNull { entry ->
        allClubs.firstOrNull { it.id == entry.clubId }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Top Header + Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DLS KIT STUDIO",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pro Jersey & Club Crest Platform",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "512×512 UV",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Interactive Search Trigger
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("home_search_bar")
                        .clickable(onClick = onSearchClicked),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search kits",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Search clubs, countries, leagues, seasons...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Hero Banner featuring Football Jerseys
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(210.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_stadium_kits),
                        contentDescription = "Football jerseys hero banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to Color(0xEE070B16),
                                    0.55f to Color(0xBB070B16),
                                    1.0f to Color(0x33070B16)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = "2025/26 SEASON READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                            Text(
                                text = copy.heroTitle,
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = copy.heroSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                maxLines = 2
                            )
                        }

                        Button(
                            onClick = onOpenKitCreator,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF070B16)
                            ),
                            modifier = Modifier.testTag("hero_create_kit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Launch Kit Studio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Access to Kit Creator, Logo Creator & Official DLS Kit Uploader
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStudioTile(
                        title = "Kit Creator",
                        subtitle = "Jersey Studio & UV",
                        icon = Icons.Default.Checkroom,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onOpenKitCreator,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_kit_creator_tile")
                    )
                    QuickStudioTile(
                        title = "Logo Creator",
                        subtitle = "Drag & Drop Crest",
                        icon = Icons.Default.Shield,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onOpenLogoCreator,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_logo_creator_tile")
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_upload_kit_link_card")
                        .clickable(onClick = onOpenKitUploader),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import DLS 26 Kit → Reverse Edit & Link",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Extract palette, re-tint, stamp crest/sponsor & host 512×512 PNG",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Open Kit Link Generator",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Featured & Trending Kits
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioSectionHeader(
                    title = copy.trendingTitle,
                    actionLabel = "See All",
                    onActionClick = { onSeeAllFilter(CatalogSpecialFilter.TRENDING) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingClubs, key = { it.id }) { club ->
                        KitShowcaseCard(
                            club = club,
                            onClick = { onClubSelected(club) },
                            modifier = Modifier.width(168.dp)
                        )
                    }
                }
            }
        }

        // Recently Added Kits
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioSectionHeader(
                    title = copy.recentTitle,
                    actionLabel = "Browse",
                    onActionClick = { onSeeAllFilter(CatalogSpecialFilter.RECENTLY_ADDED) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyAddedClubs, key = { "rec_${it.id}" }) { club ->
                        KitShowcaseCard(
                            club = club,
                            onClick = { onClubSelected(club) },
                            modifier = Modifier.width(168.dp)
                        )
                    }
                }
            }
        }

        // Featured Customization Templates
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioSectionHeader(
                    title = "${copy.templatesTitle} (${featuredTemplates.size})",
                    actionLabel = "Studio",
                    onActionClick = onOpenKitCreator
                )
                LazyRow(
                    modifier = Modifier.testTag("featured_templates_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredTemplates, key = { it.id }) { tpl ->
                        TemplateHorizontalCard(
                            template = tpl,
                            onClick = { onUseTemplate(tpl) }
                        )
                    }
                }
            }
        }

        // Goalkeeper Studio Templates
        if (gkTemplates.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudioSectionHeader(
                        title = "GK STUDIO • KEEPER KITS (${gkTemplates.size})",
                        actionLabel = "GK Studio",
                        onActionClick = onOpenGkStudio
                    )
                    LazyRow(
                        modifier = Modifier.testTag("home_gk_templates_row"),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(gkTemplates, key = { it.id }) { tpl ->
                            TemplateHorizontalCard(
                                template = tpl,
                                onClick = { onUseTemplate(tpl) }
                            )
                        }
                    }
                }
            }
        }

        // Popular Club Collections
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioSectionHeader(
                    title = copy.popularClubsTitle,
                    actionLabel = "All Clubs",
                    onActionClick = { onSeeAllFilter(CatalogSpecialFilter.ALL) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularClubCollections, key = { "pop_${it.id}" }) { club ->
                        KitShowcaseCard(
                            club = club,
                            onClick = { onClubSelected(club) },
                            modifier = Modifier.width(168.dp)
                        )
                    }
                }
            }
        }

        // National Team Collections
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioSectionHeader(
                    title = copy.nationalTeamsTitle,
                    actionLabel = "All Nations",
                    onActionClick = { onSeeAllFilter(CatalogSpecialFilter.NATIONAL_TEAMS) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nationalTeams, key = { "nat_${it.id}" }) { club ->
                        KitShowcaseCard(
                            club = club,
                            onClick = { onClubSelected(club) },
                            modifier = Modifier.width(168.dp)
                        )
                    }
                }
            }
        }

        // Recently Viewed Items (from Room)
        if (recentlyViewedBundles.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "RECENTLY VIEWED",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentlyViewedBundles, key = { "rv_${it.id}" }) { club ->
                            KitShowcaseCard(
                                club = club,
                                onClick = { onClubSelected(club) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStudioTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_studio_creator_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.22f,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateHorizontalCard(
    template: CustomizationTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(172.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                JerseyPreviewCanvas(
                    spec = template.designSpec,
                    modifier = Modifier.height(104.dp)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = template.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Customize",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
