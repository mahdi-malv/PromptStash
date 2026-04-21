package com.mahdimalv.prompstash.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.model.derivePromptTitle
import com.mahdimalv.prompstash.data.model.wordCount
import com.mahdimalv.prompstash.data.repository.PromptRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorUiState(
    val promptId: String? = null,
    val title: String = "",
    val promptBody: String = "",
    val selectedTags: Set<String> = emptySet(),
    val createdAt: Long? = null,
    val isExistingPrompt: Boolean = false,
    val isLoading: Boolean = false,
) {
    val wordCount: Int
        get() = promptBody.wordCount()
}

sealed interface EditorEvent {
    data class Message(val value: String) : EditorEvent
    data object Saved : EditorEvent
    data object Deleted : EditorEvent
}

val editorTags = listOf("Learning", "Software", "Personal", "Health")

class PromptEditorViewModel(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    private var loadedPromptId: String? = null
    private var observePromptJob: Job? = null
    private var deletedPromptId: String? = null

    fun loadPrompt(promptId: String?) {
        if (loadedPromptId == promptId && (promptId == null || uiState.value.promptId == promptId)) return

        loadedPromptId = promptId
        deletedPromptId = null
        observePromptJob?.cancel()

        if (promptId == null) {
            _uiState.value = EditorUiState()
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        observePromptJob = viewModelScope.launch {
            repository.observePrompt(promptId).collectLatest { prompt ->
                if (prompt == null) {
                    _uiState.value = EditorUiState()
                    if (deletedPromptId != promptId) {
                        _events.emit(EditorEvent.Message("Prompt not found."))
                    }
                } else {
                    _uiState.value = EditorUiState(
                        promptId = prompt.id,
                        title = prompt.title,
                        promptBody = prompt.body,
                        selectedTags = prompt.tags.toSet(),
                        createdAt = prompt.createdAt,
                        isExistingPrompt = true,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onBodyChange(text: String) {
        _uiState.update { state ->
            val updatedTitle = if (state.isExistingPrompt || state.title.isNotBlank()) {
                state.title
            } else {
                derivePromptTitle(text)
            }
            state.copy(promptBody = text, title = updatedTitle)
        }
    }

    fun onTagToggle(tag: String) {
        _uiState.update { state ->
            val tags = state.selectedTags.toMutableSet()
            if (tag in tags) tags.remove(tag) else tags.add(tag)
            state.copy(selectedTags = tags)
        }
    }

    fun save() {
        val state = _uiState.value
        val body = state.promptBody.trim()
        if (body.isBlank()) {
            viewModelScope.launch {
                _events.emit(EditorEvent.Message("Prompt body cannot be empty."))
            }
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val promptId = state.promptId ?: UUID.randomUUID().toString()
            repository.upsertPrompt(
                Prompt(
                    id = promptId,
                    title = state.title.trim().ifBlank { derivePromptTitle(body) },
                    body = body,
                    tags = state.selectedTags.toList().sorted(),
                    createdAt = state.createdAt ?: now,
                    updatedAt = now,
                )
            )
            _uiState.update {
                it.copy(
                    promptId = promptId,
                    title = it.title.trim().ifBlank { derivePromptTitle(body) },
                    promptBody = body,
                    createdAt = it.createdAt ?: now,
                    isExistingPrompt = true,
                )
            }
            _events.emit(EditorEvent.Saved)
        }
    }

    fun delete() {
        val promptId = _uiState.value.promptId ?: return
        viewModelScope.launch {
            deletedPromptId = promptId
            repository.deletePrompt(promptId)
            _events.emit(EditorEvent.Deleted)
        }
    }

    fun availableEditorTags(): List<String> = editorTags
}
