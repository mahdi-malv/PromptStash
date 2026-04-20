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

private const val MAX_PINNED_PROMPTS = 3

data class LibraryPromptItem(
    val prompt: Prompt,
    val isPinned: Boolean,
    val showPinAction: Boolean,
)

data class LibraryUiState(
    val prompts: List<Prompt> = emptyList(),
    val pinnedPromptIds: List<String> = emptyList(),
    val searchQuery: String = "",
    val isSyncing: Boolean = false,
    val selectedRemote: RemoteType = RemoteType.NONE,
    val syncStatus: SyncStatus = SyncStatus(),
) {
    private val activePinnedPromptIds: List<String>
        get() = pinnedPromptIds.filter { pinnedId ->
            prompts.any { prompt -> prompt.id == pinnedId }
        }

    val filteredPrompts: List<LibraryPromptItem>
        get() {
            val query = searchQuery.trim()
            val visiblePrompts = if (query.isBlank()) {
                prompts
            } else {
                prompts.filter { prompt ->
                    prompt.title.contains(query, ignoreCase = true) ||
                        prompt.body.contains(query, ignoreCase = true) ||
                        prompt.tags.any { it.contains(query, ignoreCase = true) }
                }
            }

            val pinnedPromptsById = visiblePrompts.associateBy(Prompt::id)
            val orderedPinnedPrompts = activePinnedPromptIds.mapNotNull(pinnedPromptsById::get)
            val pinnedPromptIdSet = activePinnedPromptIds.toSet()
            val orderedUnpinnedPrompts = visiblePrompts.filterNot { it.id in pinnedPromptIdSet }
            val showUnpinnedPinAction = activePinnedPromptIds.size < MAX_PINNED_PROMPTS

            return buildList {
                orderedPinnedPrompts.forEach { prompt ->
                    add(
                        LibraryPromptItem(
                            prompt = prompt,
                            isPinned = true,
                            showPinAction = true,
                        )
                    )
                }
                orderedUnpinnedPrompts.forEach { prompt ->
                    add(
                        LibraryPromptItem(
                            prompt = prompt,
                            isPinned = false,
                            showPinAction = showUnpinnedPinAction,
                        )
                    )
                }
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
    private val libraryState = combine(
        repository.observePrompts(),
        userPreferencesRepository.pinnedPromptIds,
        searchQuery,
        promptSyncStore.isSyncing,
    ) { prompts, pinnedPromptIds, currentQuery, isSyncing ->
        LibraryUiState(
            prompts = prompts,
            pinnedPromptIds = pinnedPromptIds,
            searchQuery = currentQuery,
            isSyncing = isSyncing,
        )
    }

    val events: SharedFlow<LibraryEvent> = _events.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        libraryState,
        userPreferencesRepository.remoteType,
        userPreferencesRepository.syncStatus,
    ) { libraryState, remoteType, syncStatus ->
        libraryState.copy(
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

    fun onPinToggle(promptId: String) {
        viewModelScope.launch {
            userPreferencesRepository.togglePinnedPrompt(promptId)
        }
    }
}
