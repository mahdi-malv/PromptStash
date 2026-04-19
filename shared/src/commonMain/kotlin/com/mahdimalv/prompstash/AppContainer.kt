package com.mahdimalv.prompstash

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.buildPromptDatabase
import com.mahdimalv.prompstash.data.repository.PromptRepository
import com.mahdimalv.prompstash.data.repository.RoomPromptRepository
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository

class AppContainer(
    val promptRepository: PromptRepository,
    val userPreferencesRepository: UserPreferencesRepository,
)

fun createAppContainer(
    databaseBuilder: RoomDatabase.Builder<PromptDatabase>,
    preferencesDataStore: DataStore<Preferences>,
): AppContainer {
    val database = buildPromptDatabase(databaseBuilder)

    return AppContainer(
        promptRepository = RoomPromptRepository(database.promptDao()),
        userPreferencesRepository = UserPreferencesRepository(preferencesDataStore),
    )
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided.")
}
