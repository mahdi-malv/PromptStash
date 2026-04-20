package com.mahdimalv.prompstash.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val onPinnedPromptsChanged: suspend () -> Unit = {},
) {
    val pinnedPromptIds: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[PinnedPromptIdsKey]
            .orEmpty()
            .split(PinnedPromptIdsSeparator)
            .map(String::trim)
            .filter(String::isNotBlank)
            .take(MaxPinnedPrompts)
    }

    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        val storedValue = preferences[ThemePreferenceKey]
        ThemePreference.entries.firstOrNull { it.name == storedValue } ?: ThemePreference.SYSTEM
    }

    val remoteType: Flow<RemoteType> = dataStore.data.map { preferences ->
        val storedValue = preferences[RemoteTypeKey]
        RemoteType.entries.firstOrNull { it.name == storedValue } ?: RemoteType.NONE
    }

    val syncStatus: Flow<SyncStatus> = dataStore.data.map { preferences ->
        SyncStatus(
            lastSyncAt = preferences[LastSyncTimestampKey],
            lastSyncWasSuccessful = preferences[LastSyncWasSuccessfulKey],
            lastSyncMessage = preferences[LastSyncMessageKey],
        )
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[ThemePreferenceKey] = themePreference.name
        }
    }

    suspend fun setRemoteType(remoteType: RemoteType) {
        dataStore.edit { preferences ->
            preferences[RemoteTypeKey] = remoteType.name
        }
    }

    suspend fun getOrCreateDeviceId(): String {
        var deviceId = ""
        dataStore.edit { preferences ->
            deviceId = preferences[DeviceIdKey] ?: UUID.randomUUID().toString().also {
                preferences[DeviceIdKey] = it
            }
        }
        return deviceId
    }

    suspend fun recordSyncSuccess(timestamp: Long, message: String = "Sync complete") {
        dataStore.edit { preferences ->
            preferences[LastSyncTimestampKey] = timestamp
            preferences[LastSyncWasSuccessfulKey] = true
            preferences[LastSyncMessageKey] = message
        }
    }

    suspend fun recordSyncFailure(timestamp: Long, message: String) {
        dataStore.edit { preferences ->
            preferences[LastSyncTimestampKey] = timestamp
            preferences[LastSyncWasSuccessfulKey] = false
            preferences[LastSyncMessageKey] = message
        }
    }

    suspend fun togglePinnedPrompt(promptId: String) {
        var didChange = false
        dataStore.edit { preferences ->
            val currentPinnedPromptIds = preferences[PinnedPromptIdsKey]
                .orEmpty()
                .split(PinnedPromptIdsSeparator)
                .map(String::trim)
                .filter(String::isNotBlank)
                .take(MaxPinnedPrompts)
                .toMutableList()

            if (currentPinnedPromptIds.remove(promptId)) {
                preferences[PinnedPromptIdsKey] = currentPinnedPromptIds.joinToString(PinnedPromptIdsSeparator)
                didChange = true
                return@edit
            }

            if (currentPinnedPromptIds.size >= MaxPinnedPrompts) {
                return@edit
            }

            currentPinnedPromptIds.add(0, promptId)
            preferences[PinnedPromptIdsKey] = currentPinnedPromptIds.joinToString(PinnedPromptIdsSeparator)
            didChange = true
        }

        if (didChange) {
            onPinnedPromptsChanged()
        }
    }

    private companion object {
        private const val MaxPinnedPrompts = 3
        private const val PinnedPromptIdsSeparator = "\n"
        val ThemePreferenceKey = stringPreferencesKey("theme_preference")
        val DeviceIdKey = stringPreferencesKey("device_id")
        val RemoteTypeKey = stringPreferencesKey("remote_type")
        val LastSyncTimestampKey = longPreferencesKey("last_sync_timestamp")
        val LastSyncWasSuccessfulKey = booleanPreferencesKey("last_sync_was_successful")
        val LastSyncMessageKey = stringPreferencesKey("last_sync_message")
        // Pinning is local-only device UI state and is intentionally not synced.
        val PinnedPromptIdsKey = stringPreferencesKey("pinned_prompt_ids")
    }
}
