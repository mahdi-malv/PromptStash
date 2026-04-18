package com.mahdimalv.prompstash.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val themePreference: Flow<ThemePreference> = context.dataStore.data.map { preferences ->
        val storedValue = preferences[ThemePreferenceKey]
        ThemePreference.entries.firstOrNull { it.name == storedValue } ?: ThemePreference.SYSTEM
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[ThemePreferenceKey] = themePreference.name
        }
    }

    private companion object {
        val ThemePreferenceKey = stringPreferencesKey("theme_preference")
    }
}
