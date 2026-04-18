package com.mahdimalv.prompstash.ui.screens.quicksave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class QuickSaveUiState(
    val promptText: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSaved: Boolean = false,
)

val availableTags = listOf("Creative Writing", "Python", "Logic", "Marketing Copy")

@HiltViewModel
class QuickSaveViewModel @Inject constructor(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickSaveUiState())
    val uiState: StateFlow<QuickSaveUiState> = _uiState.asStateFlow()

    fun onPromptTextChange(text: String) {
        _uiState.update { it.copy(promptText = text) }
    }

    fun onTagToggle(tag: String) {
        _uiState.update { state ->
            val tags = state.selectedTags.toMutableSet()
            if (tags.contains(tag)) tags.remove(tag) else tags.add(tag)
            state.copy(selectedTags = tags)
        }
    }

    fun save() {
        val text = _uiState.value.promptText.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.savePrompt(
                Prompt(
                    id = UUID.randomUUID().toString(),
                    title = text.take(50),
                    body = text,
                    tags = _uiState.value.selectedTags.toList(),
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
