@file:Suppress("DEPRECATION")

package com.mahdimalv.prompstash.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.prompstash.LocalAppContainer
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.ui.platformViewModel
import com.mahdimalv.prompstash.ui.components.FloatingNavBar
import com.mahdimalv.prompstash.ui.components.PromptCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptLibraryScreen(
    currentDestination: Any?,
    pendingMessage: String? = null,
    onPendingMessageShown: () -> Unit = {},
    onNavigateToQuickSave: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onOpenPrompt: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PromptLibraryViewModel = rememberPromptLibraryViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pendingMessage) {
        if (pendingMessage != null) {
            snackbarHostState.showSnackbar(pendingMessage)
            onPendingMessageShown()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.Message -> snackbarHostState.showSnackbar(event.value)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PrompStash",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                actions = {
                    IconButton(
                        onClick = viewModel::onSyncRequested,
                        enabled = !uiState.isSyncing,
                        modifier = Modifier.testTag("library_sync"),
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Sync,
                                contentDescription = "Sync prompts",
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToQuickSave,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Quick Save") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge,
            )
        },
        bottomBar = {
            FloatingNavBar(
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    when (dest) {
                        is com.mahdimalv.prompstash.ui.navigation.Editor -> onNavigateToEditor()
                        is com.mahdimalv.prompstash.ui.navigation.Settings -> onNavigateToSettings()
                        else -> Unit
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            TextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search title, prompt text, or tags",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search"),
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                if (uiState.searchQuery.isBlank()) "Your prompts" else "Search results",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))

            when {
                uiState.isEmpty -> {
                    EmptyState(
                        title = "No prompts yet",
                        description = "Save your first agent prompt to build a reusable library.",
                    )
                }

                uiState.hasNoSearchResults -> {
                    EmptyState(
                        title = "No matching prompts",
                        description = "Try a different word from the title, body, or tags.",
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(
                            items = uiState.filteredPrompts,
                            key = { it.prompt.id },
                        ) { promptItem ->
                            PromptCard(
                                prompt = promptItem.prompt,
                                isPinned = promptItem.isPinned,
                                showPinAction = promptItem.showPinAction,
                                onClick = { onOpenPrompt(promptItem.prompt.id) },
                                onPinToggle = { viewModel.onPinToggle(promptItem.prompt.id) },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(promptItem.prompt.body))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Copied \"${promptItem.prompt.title}\"")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberPromptLibraryViewModel(): PromptLibraryViewModel {
    val appContainer = LocalAppContainer.current
    return platformViewModel {
        PromptLibraryViewModel(
            repository = appContainer.promptRepository,
            userPreferencesRepository = appContainer.userPreferencesRepository,
            promptSyncStore = appContainer.promptSyncStore,
        )
    }
}

@Composable
private fun EmptyState(
    title: String,
    description: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
