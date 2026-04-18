package com.mahdimalv.prompstash.ui.screens.quicksave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.model.derivePromptTitle
import com.mahdimalv.prompstash.data.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class QuickSaveUiState(
    val promptText: String = "",
    val selectedTags: Set<String> = emptySet(),
)

sealed interface QuickSaveEvent {
    data object Saved : QuickSaveEvent
    data class Message(val value: String) : QuickSaveEvent
}

val availableTags = listOf("Creative Writing", "Python", "Logic", "Marketing", "Research", "Product")

@HiltViewModel
class QuickSaveViewModel @Inject constructor(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickSaveUiState())
    val uiState: StateFlow<QuickSaveUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuickSaveEvent>()
    val events: SharedFlow<QuickSaveEvent> = _events.asSharedFlow()

    fun onPromptTextChange(text: String) {
        _uiState.update { it.copy(promptText = text) }
    }

    fun onTagToggle(tag: String) {
        _uiState.update { state ->
            val tags = state.selectedTags.toMutableSet()
            if (tag in tags) tags.remove(tag) else tags.add(tag)
            state.copy(selectedTags = tags)
        }
    }

    fun save() {
        val promptText = _uiState.value.promptText.trim()
        if (promptText.isBlank()) {
            viewModelScope.launch {
                _events.emit(QuickSaveEvent.Message("Add some prompt text before saving."))
            }
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.upsertPrompt(
                Prompt(
                    id = UUID.randomUUID().toString(),
                    title = derivePromptTitle(promptText),
                    body = promptText,
                    tags = _uiState.value.selectedTags.toList().sorted(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            _events.emit(QuickSaveEvent.Saved)
        }
    }
}
