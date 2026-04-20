package com.mahdimalv.prompstash.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.settings.ThemePreference
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.sync.DropboxAuthEvent
import com.mahdimalv.prompstash.data.sync.DropboxAuthManager
import com.mahdimalv.prompstash.data.sync.DropboxAuthState
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val selectedRemote: RemoteType = RemoteType.NONE,
    val dropboxAuthState: DropboxAuthState = DropboxAuthState(),
    val syncStatus: SyncStatus = SyncStatus(),
    val isSyncing: Boolean = false,
)

sealed interface SettingsEvent {
    data class Message(val value: String) : SettingsEvent
}

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dropboxAuthManager: DropboxAuthManager,
    private val promptSyncStore: PromptSyncStore,
) : ViewModel() {
    private val _events = MutableSharedFlow<SettingsEvent>()

    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    private val settingsState = combine(
        userPreferencesRepository.themePreference,
        userPreferencesRepository.remoteType,
        userPreferencesRepository.syncStatus,
    ) { themePreference, remoteType, syncStatus ->
        Triple(themePreference, remoteType, syncStatus)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsState,
        dropboxAuthManager.authState,
        promptSyncStore.isSyncing,
    ) { settingsState, authState, isSyncing ->
        SettingsUiState(
            themePreference = settingsState.first,
            selectedRemote = settingsState.second,
            dropboxAuthState = authState,
            syncStatus = settingsState.third,
            isSyncing = isSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            dropboxAuthManager.events.collect { event ->
                when (event) {
                    is DropboxAuthEvent.Authenticated -> {
                        if (uiState.value.selectedRemote == RemoteType.NONE) {
                            userPreferencesRepository.setRemoteType(RemoteType.DROPBOX)
                        }
                    }

                    is DropboxAuthEvent.Message -> {
                        _events.emit(SettingsEvent.Message(event.value))
                    }
                }
            }
        }
    }

    fun onThemePreferenceSelected(themePreference: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.setThemePreference(themePreference)
        }
    }

    fun onRemoteTypeSelected(remoteType: RemoteType) {
        viewModelScope.launch {
            userPreferencesRepository.setRemoteType(remoteType)
        }
    }

    fun beginDropboxAuth() {
        viewModelScope.launch {
            try {
                _events.emit(SettingsEvent.Message(dropboxAuthManager.startAuthorization()))
            } catch (error: Exception) {
                _events.emit(SettingsEvent.Message(error.message ?: "Dropbox auth failed."))
            }
        }
    }

    fun removeDropboxAuth() {
        viewModelScope.launch {
            dropboxAuthManager.removeAuthentication()
        }
    }
}
