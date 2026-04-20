package com.mahdimalv.prompstash.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.PromptRepository
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SyncStatus
import com.mahdimalv.prompstash.data.sync.SyncTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val prompts: List<Prompt> = emptyList(),
    val searchQuery: String = "",
    val isSyncing: Boolean = false,
    val selectedRemote: RemoteType = RemoteType.NONE,
    val syncStatus: SyncStatus = SyncStatus(),
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

sealed interface LibraryEvent {
    data class Message(val value: String) : LibraryEvent
}

class PromptLibraryViewModel(
    repository: PromptRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val promptSyncStore: PromptSyncStore,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val _events = MutableSharedFlow<LibraryEvent>()

    val events: SharedFlow<LibraryEvent> = _events.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observePrompts(),
        searchQuery,
        promptSyncStore.isSyncing,
        userPreferencesRepository.remoteType,
        userPreferencesRepository.syncStatus,
    ) { prompts, currentQuery, isSyncing, remoteType, syncStatus ->
        LibraryUiState(
            prompts = prompts,
            searchQuery = currentQuery,
            isSyncing = isSyncing,
            selectedRemote = remoteType,
            syncStatus = syncStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.update { query }
    }

    fun onSyncRequested() {
        viewModelScope.launch {
            val result = promptSyncStore.sync(SyncTrigger.MANUAL)
            _events.emit(LibraryEvent.Message(result.message))
        }
    }
}
