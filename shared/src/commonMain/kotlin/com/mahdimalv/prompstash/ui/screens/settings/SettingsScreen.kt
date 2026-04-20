package com.mahdimalv.prompstash.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.prompstash.LocalAppContainer
import com.mahdimalv.prompstash.data.settings.ThemePreference
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.ui.platformViewModel
import com.mahdimalv.prompstash.ui.components.FloatingNavBar
import com.mahdimalv.prompstash.ui.navigation.Editor
import com.mahdimalv.prompstash.ui.navigation.Library

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentDestination: Any?,
    onNavigateToLibrary: () -> Unit,
    onNavigateToEditor: () -> Unit,
    viewModel: SettingsViewModel = rememberSettingsViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Message -> snackbarHostState.showSnackbar(event.value)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FloatingNavBar(
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    when (dest) {
                        is Library -> onNavigateToLibrary()
                        is Editor -> onNavigateToEditor()
                        else -> Unit
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
            Icon(
                Icons.Outlined.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Theme",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose how PrompStash should look on this device.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemePreference.entries.forEach { preference ->
                    AssistChip(
                        onClick = { viewModel.onThemePreferenceSelected(preference) },
                        label = {
                            Text(
                                when (preference) {
                                    ThemePreference.SYSTEM -> "System"
                                    ThemePreference.LIGHT -> "Light"
                                    ThemePreference.DARK -> "Dark"
                                }
                            )
                        },
                        leadingIcon = if (uiState.themePreference == preference) {
                            {
                                Icon(
                                    Icons.Outlined.SettingsSuggest,
                                    contentDescription = null,
                                    modifier = Modifier.padding(0.dp),
                                )
                            }
                        } else {
                            null
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (uiState.themePreference == preference) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            labelColor = if (uiState.themePreference == preference) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Icon(
                Icons.Outlined.Sync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Remote Sync",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose a remote provider and connect Dropbox securely for sync.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RemoteType.entries.forEach { remoteType ->
                    AssistChip(
                        onClick = { viewModel.onRemoteTypeSelected(remoteType) },
                        label = {
                            Text(
                                when (remoteType) {
                                    RemoteType.NONE -> "None"
                                    RemoteType.DROPBOX -> "Dropbox"
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (uiState.selectedRemote == remoteType) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            labelColor = if (uiState.selectedRemote == remoteType) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                dropboxFootnote(uiState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isAuthenticated = uiState.dropboxAuthState.isAuthenticated
                val isAuthorizing = uiState.dropboxAuthState.isAuthorizing
                if (isAuthenticated) {
                    OutlinedButton(
                        onClick = viewModel::removeDropboxAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_remove_dropbox_auth"),
                        shape = MaterialTheme.shapes.extraLarge,
                        enabled = !isAuthorizing,
                    ) {
                        Text("Remove auth")
                    }
                } else {
                    Button(
                        onClick = viewModel::beginDropboxAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_begin_dropbox_auth"),
                        shape = MaterialTheme.shapes.extraLarge,
                        enabled = !isAuthorizing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(if (isAuthorizing) "Waiting for Dropbox..." else "Auth")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            uiState.dropboxAuthState.lastErrorMessage?.let { errorMessage ->
                Text(
                    errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_dropbox_auth_error"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                syncStatusText(uiState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Version 1.0",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberSettingsViewModel(): SettingsViewModel {
    val appContainer = LocalAppContainer.current
    return platformViewModel {
        SettingsViewModel(
            userPreferencesRepository = appContainer.userPreferencesRepository,
            dropboxAuthManager = appContainer.dropboxAuthManager,
            promptSyncStore = appContainer.promptSyncStore,
        )
    }
}

private fun dropboxFootnote(uiState: SettingsUiState): String {
    val authState = uiState.dropboxAuthState
    return when {
        authState.isAuthenticated && authState.accountLabel != null ->
            "Connected to ${authState.accountLabel}. Sync uses the Dropbox app folder, the session is stored securely on this device, and you can remove access anytime."
        authState.isAuthenticated ->
            "Dropbox is connected. Sync uses the Dropbox app folder, the session is stored securely on this device, and you can remove access anytime."
        authState.isAuthorizing ->
            "Finish the Dropbox approval in your browser, then return to PrompStash."
        else ->
            "Connect Dropbox to sync through the app folder. PrompStash stores the auth session securely on this device, and you can remove access anytime."
    }
}

private fun syncStatusText(uiState: SettingsUiState): String {
    if (uiState.isSyncing) return "Sync in progress..."

    val syncStatus = uiState.syncStatus
    return when {
        syncStatus.lastSyncAt == null -> "No sync has run on this device yet."
        syncStatus.lastSyncWasSuccessful == true -> syncStatus.lastSyncMessage ?: "Last sync completed successfully."
        syncStatus.lastSyncMessage != null -> syncStatus.lastSyncMessage
        else -> "The last sync attempt failed."
    }
}
