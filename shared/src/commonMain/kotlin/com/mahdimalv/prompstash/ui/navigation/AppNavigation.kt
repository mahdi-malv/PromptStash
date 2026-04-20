package com.mahdimalv.prompstash.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorScreen
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryScreen
import com.mahdimalv.prompstash.ui.screens.settings.SettingsScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun AppNavigation() {
    val savedStateConfiguration = remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Library.serializer())
                    subclass(Editor.serializer())
                    subclass(Settings.serializer())
                }
            }
        }
    }
    val backStack = rememberNavBackStack(savedStateConfiguration, Library)
    val entryProvider = remember {
        entryProvider<NavKey> {
            entry<Library> { library ->
                PromptLibraryScreen(
                    currentDestination = library,
                    onNavigateToEditor = { backStack.add(Editor()) },
                    onOpenPrompt = { promptId -> backStack.add(Editor(promptId)) },
                    onNavigateToSettings = {
                        backStack.removeAll { it is Settings || it is Editor }
                        if (backStack.lastOrNull() != Settings) {
                            backStack.add(Settings)
                        }
                    },
                )
            }

            entry<Editor> { editor ->
                PromptEditorScreen(
                    promptId = editor.promptId,
                    currentDestination = editor,
                    onNavigateToLibrary = {
                        backStack.removeAll { it is Settings || it is Editor }
                        if (backStack.none { it is Library }) {
                            backStack.add(Library)
                        }
                    },
                    onNavigateToSettings = {
                        backStack.removeAll { it is Settings }
                        backStack.add(Settings)
                    },
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<Settings> { settings ->
                SettingsScreen(
                    currentDestination = settings,
                    onNavigateToLibrary = {
                        backStack.removeAll { it is Settings || it is Editor }
                        if (backStack.none { it is Library }) {
                            backStack.add(Library)
                        }
                    },
                )
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
        predictivePopTransitionSpec = { _ -> fadeIn() togetherWith fadeOut() },
    )
}
