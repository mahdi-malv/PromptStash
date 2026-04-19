package com.mahdimalv.prompstash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorScreen
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryScreen
import com.mahdimalv.prompstash.ui.screens.quicksave.QuickSaveScreen
import com.mahdimalv.prompstash.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<Any>(Library) }
    var pendingLibraryMessage by remember { mutableStateOf<String?>(null) }

    fun navigateTo(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }

    fun openPrompt(promptId: String) {
        backStack.add(Editor(promptId))
    }

    when (val destination = backStack.lastOrNull() ?: Library) {
        is Library -> {
            PromptLibraryScreen(
                currentDestination = destination,
                pendingMessage = pendingLibraryMessage,
                onPendingMessageShown = { pendingLibraryMessage = null },
                onNavigateToQuickSave = { backStack.add(QuickSave) },
                onNavigateToEditor = { navigateTo(Editor()) },
                onOpenPrompt = ::openPrompt,
                onNavigateToSettings = { navigateTo(Settings) },
            )
        }

        is QuickSave -> {
            QuickSaveScreen(
                onBack = { backStack.removeLastOrNull() },
                onSaved = {
                    pendingLibraryMessage = "Prompt saved"
                    backStack.removeLastOrNull()
                },
            )
        }

        is Editor -> {
            PromptEditorScreen(
                promptId = destination.promptId,
                currentDestination = destination,
                onNavigateToLibrary = { navigateTo(Library) },
                onNavigateToSettings = { navigateTo(Settings) },
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    } else {
                        navigateTo(Library)
                    }
                },
                onPromptDeleted = {
                    pendingLibraryMessage = "Prompt deleted"
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    } else {
                        navigateTo(Library)
                    }
                },
            )
        }

        is Settings -> {
            SettingsScreen(
                currentDestination = destination,
                onNavigateToLibrary = { navigateTo(Library) },
                onNavigateToEditor = { navigateTo(Editor()) },
            )
        }
    }
}
