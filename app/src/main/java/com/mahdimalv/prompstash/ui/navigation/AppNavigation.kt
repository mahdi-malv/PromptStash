package com.mahdimalv.prompstash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorScreen
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryScreen
import com.mahdimalv.prompstash.ui.screens.quicksave.QuickSaveScreen
import com.mahdimalv.prompstash.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val backStack = remember { mutableStateListOf<Any>(Library) }

    fun navigateTo(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }

    fun openPrompt(promptId: String) {
        backStack.add(Editor(promptId))
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Library> {
                PromptLibraryScreen(
                    currentDestination = backStack.lastOrNull(),
                    onNavigateToQuickSave = { backStack.add(QuickSave) },
                    onNavigateToEditor = { navigateTo(Editor()) },
                    onOpenPrompt = ::openPrompt,
                    onNavigateToSettings = { navigateTo(Settings) },
                )
            }
            entry<QuickSave> {
                QuickSaveScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Editor> { editor ->
                PromptEditorScreen(
                    promptId = editor.promptId,
                    currentDestination = backStack.lastOrNull(),
                    onNavigateToLibrary = { navigateTo(Library) },
                    onNavigateToSettings = { navigateTo(Settings) },
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        } else {
                            navigateTo(Library)
                        }
                    },
                )
            }
            entry<Settings> {
                SettingsScreen(
                    currentDestination = backStack.lastOrNull(),
                    onNavigateToLibrary = { navigateTo(Library) },
                    onNavigateToEditor = { navigateTo(Editor()) },
                )
            }
        }
    )
}
