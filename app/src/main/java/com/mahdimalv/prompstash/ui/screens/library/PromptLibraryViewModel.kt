package com.mahdimalv.prompstash.ui.screens.library

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
import javax.inject.Inject

data class LibraryUiState(
    val prompts: List<Prompt> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
) {
    val filteredPrompts: List<Prompt>
        get() = if (searchQuery.isBlank()) prompts
        else prompts.filter { it.title.contains(searchQuery, ignoreCase = true) }
}

@HiltViewModel
class PromptLibraryViewModel @Inject constructor(
    private val repository: PromptRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState(isLoading = true))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        viewModelScope.launch {
            val prompts = repository.getPrompts()
            _uiState.update { it.copy(prompts = prompts, isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
