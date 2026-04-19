package com.mahdimalv.prompstash.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        val storedValue = preferences[ThemePreferenceKey]
        ThemePreference.entries.firstOrNull { it.name == storedValue } ?: ThemePreference.SYSTEM
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[ThemePreferenceKey] = themePreference.name
        }
    }

    private companion object {
        val ThemePreferenceKey = stringPreferencesKey("theme_preference")
    }
}
