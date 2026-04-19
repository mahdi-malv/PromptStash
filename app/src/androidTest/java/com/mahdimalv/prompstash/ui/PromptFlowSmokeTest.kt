package com.mahdimalv.prompstash.ui

import android.content.ClipboardManager
import android.content.Context
import java.io.File
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
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
import com.mahdimalv.prompstash.AppContainer
import com.mahdimalv.prompstash.PrompStashApp
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            val appContainer = remember { createSmokeAppContainer(context, repository) }
            PrompStashApp(appContainer = appContainer)
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

private fun createSmokeAppContainer(
    context: Context,
    repository: TestPromptRepository,
): AppContainer {
    val preferencesFile = File(context.cacheDir, "smoke-test.preferences_pb")
    preferencesFile.delete()

    return AppContainer(
        promptRepository = repository,
        userPreferencesRepository = UserPreferencesRepository(
            createPreferencesDataStore(producePath = { preferencesFile.absolutePath })
        ),
    )
}
