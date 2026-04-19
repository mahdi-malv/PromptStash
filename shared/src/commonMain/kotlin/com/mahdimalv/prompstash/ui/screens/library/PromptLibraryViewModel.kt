package com.mahdimalv.prompstash.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LibraryUiState(
    val prompts: List<Prompt> = emptyList(),
    val searchQuery: String = "",
) {
    val filteredPrompts: List<Prompt>
        get() {
            val query = searchQuery.trim()
            if (query.isBlank()) return prompts

            return prompts.filter { prompt ->
                prompt.title.contains(query, ignoreCase = true) ||
                    prompt.body.contains(query, ignoreCase = true) ||
                    prompt.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

    val isEmpty: Boolean
        get() = prompts.isEmpty()

    val hasNoSearchResults: Boolean
        get() = searchQuery.isNotBlank() && filteredPrompts.isEmpty()
}

class PromptLibraryViewModel(
    repository: PromptRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observePrompts(),
        searchQuery,
    ) { prompts, currentQuery ->
        LibraryUiState(
            prompts = prompts,
            searchQuery = currentQuery,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }
}
