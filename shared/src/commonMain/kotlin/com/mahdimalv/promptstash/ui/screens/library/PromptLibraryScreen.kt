@file:Suppress("DEPRECATION")

package com.mahdimalv.promptstash.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahdimalv.promptstash.LocalAppContainer
import com.mahdimalv.promptstash.ui.platformViewModel
import com.mahdimalv.promptstash.ui.components.FloatingNavBar
import com.mahdimalv.promptstash.ui.components.PromptCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptLibraryScreen(
    currentDestination: Any?,
    pendingMessage: String? = null,
    onPendingMessageShown: () -> Unit = {},
    onNavigateToEditor: () -> Unit,
    onOpenPrompt: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PromptLibraryViewModel = rememberPromptLibraryViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val supportsRemoteSync = LocalAppContainer.current.platformCapabilities.supportsRemoteSync
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var navBarHeight by remember { mutableStateOf(0.dp) }
    var topBarHeight by remember { mutableStateOf(0.dp) }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    var showFab by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (idx, off) ->
            val scrollingDown = when {
                idx > prevIndex -> true
                idx < prevIndex -> false
                else -> off > prevOffset
            }
            val atTop = idx == 0 && off == 0
            showFab = !scrollingDown || atTop
            prevIndex = idx
            prevOffset = off
        }
    }

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
                LibraryEvent.ScrollToTop -> listState.animateScrollToItem(0)
            }
        }
    }

    val syncAction: @Composable () -> Unit = {
        if (supportsRemoteSync) {
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
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isEmpty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = navBarHeight)
                            .padding(horizontal = 16.dp),
                    ) {
                        EmptyState(
                            title = "Your stash is empty.",
                            description = "Stash your first prompt to reuse it anywhere.",
                        )
                    }
                }

                uiState.hasNoSearchResults -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = navBarHeight)
                            .padding(horizontal = 16.dp),
                    ) {
                        EmptyState(
                            title = "No matching prompts",
                            description = "Try a different word from the title, body, or tags.",
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            top = topBarHeight,
                            bottom = 24.dp + navBarHeight,
                        ),
                    ) {
                        item(key = "__section_header__") {
                            Column {
                                Text(
                                    if (uiState.searchQuery.isBlank()) "All prompts" else "Search results",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
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
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        topBarHeight = with(density) { size.height.toDp() }
                    },
            ) {
                Column {
                    AnimatedVisibility(
                        visible = !isScrolled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    "PromptStash",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            actions = { syncAction() },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search prompts…",
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
                                .weight(1f)
                                .testTag("library_search"),
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true,
                        )
                        AnimatedVisibility(
                            visible = isScrolled,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut(),
                        ) {
                            syncAction()
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn() + fadeIn() + slideInHorizontally { it },
                exit = scaleOut() + fadeOut() + slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = navBarHeight),
            ) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToEditor,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("New") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.extraLarge,
                )
            }
            FloatingNavBar(
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    when (dest) {
                        is com.mahdimalv.promptstash.ui.navigation.Editor -> onNavigateToEditor()
                        is com.mahdimalv.promptstash.ui.navigation.Settings -> onNavigateToSettings()
                        else -> Unit
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { size ->
                        navBarHeight = with(density) { size.height.toDp() }
                    },
            )
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
                style = MaterialTheme.typography.headlineMedium,
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
