package com.mahdimalv.prompstash.ui

import com.mahdimalv.prompstash.MainDispatcherRule
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.ui.screens.editor.PromptEditorViewModel
import com.mahdimalv.prompstash.ui.screens.library.PromptLibraryViewModel
import com.mahdimalv.prompstash.ui.screens.quicksave.QuickSaveViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PromptViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setUp() {
        mainDispatcherRule.starting()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.finished()
    }

    @Test
    fun librarySearchMatchesTitleBodyAndTags() = runTest {
        val repository = TestPromptRepository(
            initialPrompts = listOf(
                prompt(
                    id = "1",
                    title = "Creative Story Prompt",
                    body = "Write a story about a comet.",
                    tags = listOf("creative"),
                    updatedAt = 2L,
                ),
                prompt(
                    id = "2",
                    title = "Refactor Notes",
                    body = "Review Kotlin code for null safety.",
                    tags = listOf("code", "kotlin"),
                    updatedAt = 3L,
                ),
            )
        )

        val viewModel = PromptLibraryViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("kotlin")
        advanceUntilIdle()

        assertEquals(listOf("2"), viewModel.uiState.value.filteredPrompts.map(Prompt::id))
    }

    @Test
    fun quickSaveCreatesPrompt() = runTest {
        val repository = TestPromptRepository()
        val viewModel = QuickSaveViewModel(repository)

        viewModel.onPromptTextChange("Kotlin Code Review Prompt\nReview Kotlin for readability.")
        viewModel.onTagToggle("Research")
        viewModel.save()
        advanceUntilIdle()

        val savedPrompt = repository.observePrompts().first().single()
        assertEquals("Kotlin Code Review Prompt", savedPrompt.title)
        assertEquals(listOf("Research"), savedPrompt.tags)
        assertTrue(savedPrompt.body.contains("Review Kotlin"))
    }

    @Test
    fun editorUpdatesExistingPrompt() = runTest {
        val repository = TestPromptRepository(
            initialPrompts = listOf(
                prompt(
                    id = "existing",
                    title = "Old title",
                    body = "Old body",
                    tags = listOf("Logic"),
                    createdAt = 10L,
                    updatedAt = 10L,
                )
            )
        )
        val viewModel = PromptEditorViewModel(repository)

        viewModel.loadPrompt("existing")
        advanceUntilIdle()
        viewModel.onTitleChange("Updated title")
        viewModel.onBodyChange("Updated body")
        viewModel.onTagToggle("Logic")
        viewModel.onTagToggle("Python")
        viewModel.save()
        advanceUntilIdle()

        val updatedPrompt = repository.observePrompt("existing").first()
        requireNotNull(updatedPrompt)
        assertEquals("Updated title", updatedPrompt.title)
        assertEquals("Updated body", updatedPrompt.body)
        assertEquals(listOf("Python"), updatedPrompt.tags)
        assertEquals(10L, updatedPrompt.createdAt)
        assertTrue(updatedPrompt.updatedAt >= 10L)
    }

    @Test
    fun deleteRemovesPromptFromObservedList() = runTest {
        val repository = TestPromptRepository(
            initialPrompts = listOf(
                prompt(
                    id = "existing",
                    title = "Prompt to delete",
                    body = "Body",
                    updatedAt = 1L,
                )
            )
        )
        val libraryViewModel = PromptLibraryViewModel(repository)
        val editorViewModel = PromptEditorViewModel(repository)

        backgroundScope.launch { libraryViewModel.uiState.collect {} }
        editorViewModel.loadPrompt("existing")
        advanceUntilIdle()
        editorViewModel.delete()
        advanceUntilIdle()

        assertTrue(libraryViewModel.uiState.value.prompts.isEmpty())
    }

    private fun prompt(
        id: String,
        title: String,
        body: String,
        tags: List<String> = emptyList(),
        createdAt: Long = 1L,
        updatedAt: Long = createdAt,
    ): Prompt = Prompt(
        id = id,
        title = title,
        body = body,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
