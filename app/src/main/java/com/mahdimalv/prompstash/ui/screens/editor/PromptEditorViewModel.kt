package com.mahdimalv.prompstash.ui.screens.editor

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

data class EditorUiState(
    val promptBody: String = "Act as a world-class literary editor with 20 years of experience. Provide feedback focusing on:\n\n1. Narrative Hook\n2. Character Agency\n3. Thematic Resonance\n\nFormat your response as an editorial letter, maintaining an encouraging yet rigorous tone.",
    val selectedCategory: String = "Creative Writing",
    val isPublic: Boolean = true,
    val isSaved: Boolean = false,
) {
    val wordCount: Int get() = promptBody.trim().split("\\s+".toRegex()).size
}

val editorCategories = listOf("Creative Writing", "Code Gen")

@HiltViewModel
class PromptEditorViewModel @Inject constructor(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun onBodyChange(text: String) {
        _uiState.update { it.copy(promptBody = text) }
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun save() {
        viewModelScope.launch {
            repository.savePrompt(
                Prompt(
                    id = UUID.randomUUID().toString(),
                    title = _uiState.value.promptBody.take(50),
                    body = _uiState.value.promptBody,
                    tags = listOf(_uiState.value.selectedCategory),
                    isPublic = _uiState.value.isPublic,
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
