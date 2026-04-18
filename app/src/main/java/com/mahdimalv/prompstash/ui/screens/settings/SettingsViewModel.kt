package com.mahdimalv.prompstash.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val followSystemTheme: Boolean = true,
    val autoBackupEnabled: Boolean = true,
    val typographyScale: String = "Editorial (Default)",
    val categories: List<String> = listOf("Creative Writing", "Code Generation"),
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onFollowSystemThemeToggle(enabled: Boolean) {
        _uiState.update { it.copy(followSystemTheme = enabled) }
    }

    fun onAutoBackupToggle(enabled: Boolean) {
        _uiState.update { it.copy(autoBackupEnabled = enabled) }
    }
}
