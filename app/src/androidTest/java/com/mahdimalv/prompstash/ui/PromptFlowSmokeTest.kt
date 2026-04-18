package com.mahdimalv.prompstash.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mahdimalv.prompstash.data.repository.PromptRepository
import com.mahdimalv.prompstash.data.settings.ThemePreference
import com.mahdimalv.prompstash.ui.navigation.Editor
import com.mahdimalv.prompstash.ui.navigation.Library
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorScreen
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorViewModel
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryScreen
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryViewModel
import com.mahdimalv.prompstash.ui.screens.quicksave.QuickSaveScreen
import com.mahdimalv.prompstash.ui.screens.quicksave.QuickSaveViewModel
import com.mahdimalv.prompstash.ui.theme.PrompStashTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromptFlowSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun createCopyEditAndFindPrompt() {
        val repository = TestPromptRepository()

        composeRule.setContent {
            PrompStashTheme(themePreference = ThemePreference.SYSTEM) {
                SmokeHarness(repository = repository)
            }
        }

        composeRule.onNodeWithText("Quick Save").performClick()
        composeRule.onNodeWithTag("quick_save_input").performTextInput(
            "Kotlin Code Review Prompt\nReview Kotlin code for readability."
        )
        composeRule.onNodeWithTag("quick_save_submit").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription("Copy prompt").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Kotlin Code Review Prompt").assertIsDisplayed()

        composeRule.onAllNodesWithContentDescription("Copy prompt")[0].performClick()
        val clipboard = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(
            "Kotlin Code Review Prompt\nReview Kotlin code for readability.",
            clipboard.primaryClip?.getItemAt(0)?.text?.toString(),
        )

        composeRule.onNodeWithText("Kotlin Code Review Prompt").performClick()
        composeRule.onNodeWithTag("editor_title_input").performTextClearance()
        composeRule.onNodeWithTag("editor_title_input").performTextInput("Kotlin Reviewer")
        composeRule.onNodeWithTag("editor_body_input").performTextClearance()
        composeRule.onNodeWithTag("editor_body_input").performTextInput("Updated prompt body for Kotlin reviewer")
        composeRule.onNodeWithTag("editor_save").performClick()
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.onNodeWithTag("library_search").performTextInput("reviewer")
        composeRule.onNodeWithText("Kotlin Reviewer").assertIsDisplayed()
    }
}

private sealed interface SmokeScreen {
    data object LibraryScreen : SmokeScreen
    data object QuickSaveScreen : SmokeScreen
    data class EditorScreen(val promptId: String? = null) : SmokeScreen
}

@Composable
private fun SmokeHarness(
    repository: PromptRepository,
) {
    var screen by remember { mutableStateOf<SmokeScreen>(SmokeScreen.LibraryScreen) }

    val libraryViewModel = remember { PromptLibraryViewModel(repository) }
    val quickSaveViewModel = remember { QuickSaveViewModel(repository) }
    val editorViewModel = remember { PromptEditorViewModel(repository) }

    when (val currentScreen = screen) {
        SmokeScreen.LibraryScreen -> PromptLibraryScreen(
            currentDestination = Library,
            onNavigateToQuickSave = { screen = SmokeScreen.QuickSaveScreen },
            onNavigateToEditor = { screen = SmokeScreen.EditorScreen() },
            onOpenPrompt = { promptId -> screen = SmokeScreen.EditorScreen(promptId) },
            onNavigateToSettings = {},
            viewModel = libraryViewModel,
        )

        SmokeScreen.QuickSaveScreen -> QuickSaveScreen(
            onBack = { screen = SmokeScreen.LibraryScreen },
            viewModel = quickSaveViewModel,
        )

        is SmokeScreen.EditorScreen -> PromptEditorScreen(
            promptId = currentScreen.promptId,
            currentDestination = Editor(currentScreen.promptId),
            onNavigateToLibrary = { screen = SmokeScreen.LibraryScreen },
            onNavigateToSettings = {},
            onBack = { screen = SmokeScreen.LibraryScreen },
            viewModel = editorViewModel,
        )
    }
}
