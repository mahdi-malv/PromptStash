package com.mahdimalv.prompstash.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahdimalv.prompstash.data.settings.ThemePreference
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SecureCredentialStore
import com.mahdimalv.prompstash.data.sync.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val selectedRemote: RemoteType = RemoteType.NONE,
    val hasDropboxToken: Boolean = false,
    val dropboxTokenInput: String = "",
    val syncStatus: SyncStatus = SyncStatus(),
    val isSyncing: Boolean = false,
)

sealed interface SettingsEvent {
    data class Message(val value: String) : SettingsEvent
}

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val secureCredentialStore: SecureCredentialStore,
    private val promptSyncStore: PromptSyncStore,
) : ViewModel() {

    private val dropboxTokenInput = MutableStateFlow("")
    private val hasDropboxToken = MutableStateFlow(false)
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
        dropboxTokenInput,
        hasDropboxToken,
        promptSyncStore.isSyncing,
    ) { settingsState, tokenInput, hasToken, isSyncing ->
        SettingsUiState(
            themePreference = settingsState.first,
            selectedRemote = settingsState.second,
            hasDropboxToken = hasToken,
            dropboxTokenInput = tokenInput,
            syncStatus = settingsState.third,
            isSyncing = isSyncing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        refreshDropboxTokenState()
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

    fun onDropboxTokenInputChange(value: String) {
        dropboxTokenInput.value = value
    }

    fun saveDropboxToken() {
        val token = dropboxTokenInput.value.trim()
        if (token.isBlank()) {
            viewModelScope.launch {
                _events.emit(SettingsEvent.Message("Paste a Dropbox access token before saving it."))
            }
            return
        }

        viewModelScope.launch {
            secureCredentialStore.saveAccessToken(RemoteType.DROPBOX, token)
            hasDropboxToken.value = true
            dropboxTokenInput.value = ""
            if (uiState.value.selectedRemote == RemoteType.NONE) {
                userPreferencesRepository.setRemoteType(RemoteType.DROPBOX)
            }
            _events.emit(SettingsEvent.Message("Dropbox token saved securely."))
        }
    }

    fun removeDropboxToken() {
        viewModelScope.launch {
            secureCredentialStore.clearAccessToken(RemoteType.DROPBOX)
            hasDropboxToken.value = false
            dropboxTokenInput.value = ""
            _events.emit(SettingsEvent.Message("Dropbox token removed."))
        }
    }

    private fun refreshDropboxTokenState() {
        viewModelScope.launch {
            hasDropboxToken.value = !secureCredentialStore.readAccessToken(RemoteType.DROPBOX).isNullOrBlank()
        }
    }
}
