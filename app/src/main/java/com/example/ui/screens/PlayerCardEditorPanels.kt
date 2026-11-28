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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DlsPlayerCardSpec
import com.example.data.model.PlayerCardTier
import com.example.data.model.PlayerCountry
import com.example.data.model.PlayerPosition
import com.example.data.model.PlayerPositionGroup
import com.example.data.model.PreferredFoot
import com.example.data.model.dlsStatColorHex
import com.example.data.model.parseColorHex
import com.example.ui.components.ColorPaletteRow
import com.example.ui.viewmodel.PlayerCardUiState
import com.example.ui.viewmodel.PlayerCardViewModel
import kotlin.math.roundToInt

/**
 * Tab 1: Searchable 100-Player Database with Position Group Filters
 */
@Composable
fun PlayerDatabaseBrowserPanel(
    state: PlayerCardUiState,
    viewModel: PlayerCardViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("Search 100 players (name, country, CF, GK...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search players") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_db_search_input")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PlayerPositionGroup.entries) { group ->
                val count = if (group == PlayerPositionGroup.ALL) {
                    state.allPlayers.size
                } else {
                    state.allPlayers.count { it.position.group == group }
                }
                FilterChip(
                    selected = state.positionGroup == group,
                    onClick = { viewModel.setPositionGroup(group) },
                    label = { Text("${group.label} ($count)") },
                    modifier = Modifier.testTag("player_filter_${group.name}")
                )
            }
        }

        val players = state.filteredPlayers
        if (players.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No players match \"${state.searchQuery}\". Try another name or tap + New to create a custom player.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("player_db_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    val isSelected = player.id == state.activeCard.id
                    PlayerDatabaseRow(
                        player = player,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectPlayer(player, openEditor = false) },
                        onEdit = { viewModel.selectPlayer(player, openEditor = true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerDatabaseRow(
    player: DlsPlayerCardSpec,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_row_${player.id}")
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rating Circle Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(parseColorHex(player.tier.primaryHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.rating.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = parseColorHex(player.tier.textColorHex)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = parseColorHex(player.position.badgeColorHex)
                    ) {
                        Text(
                            text = player.position.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "${player.country.displayName} • ${player.heightCm}cm • ${player.preferredFoot.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedButton(
                onClick = onEdit,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ${player.fullName}", modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Tab 2: Card Identity, Tier Style, Position, Country Flag & Player Photo Upload
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerCardIdentityPanel(
    card: DlsPlayerCardSpec,
    viewModel: PlayerCardViewModel,
    onPickPhoto: () -> Unit,
    onSaveToDb: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_identity_panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = card.fullName,
                onValueChange = viewModel::setFullName,
                label = { Text("Full Name (Top Banner)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("player_full_name_input")
            )
            OutlinedTextField(
                value = card.cardShortName,
                onValueChange = viewModel::setCardShortName,
                label = { Text("Card Name") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("player_short_name_input")
            )
        }

        // Photo Upload + Positioning Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Player Portrait Photo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (card.customPhotoUri != null) {
                        TextButton(onClick = { viewModel.setCustomPhotoUri(null) }) {
                            Text("Reset Avatar")
                        }
                    }
                }
                OutlinedButton(
                    onClick = onPickPhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_upload_photo_button")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (card.customPhotoUri != null) "Change Player Photo / PNG" else "Upload Player Photo or Cutout PNG")
                }

                if (card.customPhotoUri != null) {
                    CompactSliderRow("Move X", card.photoOffsetX, -1f..1f, "${(card.photoOffsetX * 100).roundToInt()}%", viewModel::setPhotoOffsetX)
                    CompactSliderRow("Move Y", card.photoOffsetY, -1f..1f, "${(card.photoOffsetY * 100).roundToInt()}%", viewModel::setPhotoOffsetY)
                    CompactSliderRow("Zoom", card.photoScale, 0.6f..2.2f, "${(card.photoScale * 100).roundToInt()}%", viewModel::setPhotoScale)
                } else {
                    ColorPaletteRow(
                        label = "Avatar Jersey Color",
                        selectedHex = card.clubAccentHex,
                        onColorSelected = viewModel::setClubAccentHex
                    )
                }
            }
        }

        // Overall Rating + Boosted Ring + Maxed Card Quick Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Rating: ${card.rating}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = viewModel::applyMaxedBoost,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("player_max_boost_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Max +10", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Slider(
                    value = card.rating.toFloat(),
                    onValueChange = { viewModel.setRating(it.roundToInt()) },
                    valueRange = 40f..100f
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Boosted Cyan/Green Rating Ring", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = card.ratingBoosted,
                        onCheckedChange = viewModel::setRatingBoosted
                    )
                }
            }
        }

        // Card Tier Selector
        Text("Card Tier Style", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayerCardTier.entries.forEach { tier ->
                FilterChip(
                    selected = card.tier == tier,
                    onClick = { viewModel.setTier(tier) },
                    label = { Text(tier.label) },
                    modifier = Modifier.testTag("card_tier_${tier.name}")
                )
            }
        }

        // Position Selector
        Text("Position", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayerPosition.entries.forEach { pos ->
                FilterChip(
                    selected = card.position == pos,
                    onClick = { viewModel.setPosition(pos) },
                    label = { Text(pos.code) },
                    modifier = Modifier.testTag("card_pos_${pos.code}")
                )
            }
        }

        // Country Flag Selector
        Text("Country Flag (${PlayerCountry.entries.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PlayerCountry.entries) { country ->
                FilterChip(
                    selected = card.country == country,
                    onClick = { viewModel.setCountry(country) },
                    label = { Text("${country.code} • ${country.displayName}") }
                )
            }
        }

        // Height, Foot & Stamina Bar
        CompactSliderRow(
            label = "Height",
            value = card.heightCm.toFloat(),
            range = 155f..210f,
            valueLabel = "${card.heightCm}cm",
            onValueChange = { viewModel.setHeightCm(it.roundToInt()) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Foot", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(64.dp))
            PreferredFoot.entries.forEach { foot ->
                FilterChip(
                    selected = card.preferredFoot == foot,
                    onClick = { viewModel.setPreferredFoot(foot) },
                    label = { Text(foot.label) }
                )
            }
        }

        CompactSliderRow(
            label = "Energy",
            value = card.staminaPercent,
            range = 0.25f..1f,
            valueLabel = "${(card.staminaPercent * 100).roundToInt()}%",
            onValueChange = viewModel::setStaminaPercent
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSaveToDb,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_player_db_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save to List", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = viewModel::resetActiveToOriginalDatabase,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reset Base")
            }
        }
    }
}

/**
 * Tab 3: 8 DLS Attribute Sliders with Color-Coded Circular Preview Badges
 */
@Composable
fun PlayerCardStatsPanel(
    card: DlsPlayerCardSpec,
    viewModel: PlayerCardViewModel,
    onSaveToDb: () -> Unit
) {
    val isGk = card.position.isGoalkeeper
    val statEntries = listOf(
        Triple("SPE", "Speed", card.spe),
        Triple("ACC", "Acceleration", card.acc),
        Triple("STA", "Stamina", card.sta),
        Triple("STR", "Strength", card.str),
        Triple("CON", "Ball Control", card.con),
        Triple("PAS", "Passing", card.pas),
        if (isGk) Triple("GKH", "GK Handling", card.gkh) else Triple("SHO", "Shooting", card.sho),
        if (isGk) Triple("GKR", "GK Reactions", card.gkr) else Triple("TAC", "Tackling", card.tac)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_stats_panel"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGk) "Goalkeeper Attributes (8)" else "Outfield Attributes (8)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = viewModel::applyMaxedBoost,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Max All +10", style = MaterialTheme.typography.labelSmall)
            }
        }

        statEntries.forEach { (code, fullLabel, value) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF07090C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = parseColorHex(dlsStatColorHex(value))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$code • $fullLabel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = value.toFloat(),
                            onValueChange = { viewModel.setStat(code, it.roundToInt()) },
                            valueRange = 1f..100f,
                            modifier = Modifier.height(26.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setStat(code, value - 1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease $code", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setStat(code, value + 1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase $code", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Button(
            onClick = onSaveToDb,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Save Updated Stats to Database", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        )
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}
