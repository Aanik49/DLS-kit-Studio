package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.AppLanguage
import com.example.ui.theme.ThemePreference
import com.example.ui.viewmodel.MainStudioViewModel

@Composable
fun ProfileScreen(
    viewModel: MainStudioViewModel,
    onOpenMyDesigns: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val savedKits by viewModel.savedKits.collectAsStateWithLifecycle()
    val savedLogos by viewModel.savedLogos.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDlsGuideDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "STUDIO PROFILE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Guest Studio Profile Card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prefs.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${prefs.clubTagline} • Guest Studio Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChipCard(
                    value = savedKits.size.toString(),
                    label = "Saved Kits",
                    onClick = { onOpenMyDesigns(0) },
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    value = savedLogos.size.toString(),
                    label = "Created Logos",
                    onClick = { onOpenMyDesigns(1) },
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    value = favorites.size.toString(),
                    label = "Favorite Clubs",
                    onClick = { onOpenMyDesigns(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // My Designs Library Shortcut
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_my_designs_card")
                    .clickable { onOpenMyDesigns(0) },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Open My Designs Library",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Manage saved kits, logos, favorites & history (${prefs.exportCount} exports)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Theme Settings
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Appearance (Dark / Light Mode)", style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePreference.entries.forEach { mode ->
                            FilterChip(
                                selected = prefs.themePreference == mode,
                                onClick = { viewModel.setThemePreference(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_chip_${mode.name}")
                            )
                        }
                    }
                }
            }
        }

        // Language Settings
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text("Language", style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = prefs.language == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(lang.displayName, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // App Actions List
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    ProfileActionRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "How to Import 512×512 DLS Kits",
                        onClick = { showDlsGuideDialog = true }
                    )
                    ProfileActionRow(
                        icon = Icons.Default.Favorite,
                        title = "My Favorite Clubs (${favorites.size})",
                        onClick = { onOpenMyDesigns(2) }
                    )
                    ProfileActionRow(
                        icon = Icons.Default.Share,
                        title = "Share DLS Kit Studio",
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Design custom Dream League Soccer kits and team logos with DLS Kit Studio!"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share DLS Kit Studio"))
                        }
                    )
                    ProfileActionRow(
                        icon = Icons.Default.Info,
                        title = "About DLS Kit Studio",
                        onClick = { showAboutDialog = true }
                    )
                    ProfileActionRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = { showPrivacyDialog = true }
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(prefs.displayName) }
        var taglineInput by remember { mutableStateOf(prefs.clubTagline) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Studio Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Designer / Coach Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = taglineInput,
                        onValueChange = { taglineInput = it },
                        label = { Text("Studio Tagline") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateProfile(nameInput, taglineInput)
                    showEditProfileDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDlsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showDlsGuideDialog = false },
            title = { Text("Importing DLS Kits & Logos") },
            text = {
                Text(
                    "1. Browse any club in Kits or design your own in Kit Creator.\n" +
                        "2. Tap Copy URL for hosted kits, or Export 512×512 DLS UV PNG to save an authentic UV texture sheet to Pictures/DLSKitStudio.\n" +
                        "3. In Dream League Soccer, go to My Club → Customize → Edit Kit → Download and paste your kit URL.\n" +
                        "4. For custom crests, export a 512×512 Transparent PNG from Logo Creator."
                )
            },
            confirmButton = {
                Button(onClick = { showDlsGuideDialog = false }) { Text("Got it") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About DLS Kit Studio") },
            text = {
                Text(stringResource(id = R.string.about_description))
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(stringResource(id = R.string.privacy_policy_summary))
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun StatChipCard(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
