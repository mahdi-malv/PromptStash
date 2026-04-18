package com.mahdimalv.prompstash.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.prompstash.ui.components.FloatingNavBar
import com.mahdimalv.prompstash.ui.navigation.Editor
import com.mahdimalv.prompstash.ui.navigation.Library

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentDestination: Any?,
    onNavigateToLibrary: () -> Unit,
    onNavigateToEditor: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prompt Lab",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            FloatingNavBar(
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    when (dest) {
                        is Library -> onNavigateToLibrary()
                        is Editor -> onNavigateToEditor()
                        else -> {}
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))

            SectionHeader("Appearance")
            Spacer(Modifier.height(8.dp))
            SettingsListItem(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Brightness4,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                headlineText = "Theme Mode",
                supportingText = "Follow system preferences",
                trailingContent = {
                    Switch(
                        checked = uiState.followSystemTheme,
                        onCheckedChange = viewModel::onFollowSystemThemeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                },
            )
            SettingsListItem(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.FormatSize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                headlineText = "Typography Scale",
                supportingText = uiState.typographyScale,
                trailingContent = {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("Prompt Categories")
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {}) {
                Text("Manage All", color = MaterialTheme.colorScheme.primary)
            }
            uiState.categories.forEachIndexed { index, category ->
                val icon = if (index == 0) Icons.Outlined.RocketLaunch else Icons.Outlined.Terminal
                SettingsListItem(
                    leadingIcon = {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    headlineText = category,
                )
            }
            TextButton(onClick = {}) {
                Text("+ Create New Category", color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Data & Backup")
            Spacer(Modifier.height(8.dp))
            SettingsListItem(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                headlineText = "Auto-backup to Cloud",
                supportingText = "Last synced 2 hours ago",
                trailingContent = {
                    Switch(
                        checked = uiState.autoBackupEnabled,
                        onCheckedChange = viewModel::onAutoBackupToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                },
            )
            SettingsListItem(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                headlineText = "Export Workspace",
                supportingText = "JSON, CSV, Markdown",
                trailingContent = {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            SettingsListItem(
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                headlineText = "Clear Local Cache",
                supportingText = "1.2 GB of temporary data",
                trailingContent = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            Spacer(Modifier.height(32.dp))
            Text(
                "Prompt Lab v2.4.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                "Fluid Manuscript Design System",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SettingsListItem(
    headlineText: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(headlineText, style = MaterialTheme.typography.bodyLarge)
        },
        leadingContent = leadingIcon,
        supportingContent = supportingText?.let {
            { Text(it, style = MaterialTheme.typography.bodySmall) }
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
