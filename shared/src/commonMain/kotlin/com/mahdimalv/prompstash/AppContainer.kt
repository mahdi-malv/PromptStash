package com.mahdimalv.prompstash

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.buildPromptDatabase
import com.mahdimalv.prompstash.data.repository.PromptRepository
import com.mahdimalv.prompstash.data.repository.RoomPromptRepository
import com.mahdimalv.prompstash.data.repository.SyncingPromptRepository
import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.sync.DropboxPromptSyncRemote
import com.mahdimalv.prompstash.data.sync.PromptSyncCoordinator
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.RoomPromptSyncLocalStore
import com.mahdimalv.prompstash.data.sync.SecureCredentialStore
import com.mahdimalv.prompstash.data.sync.createPlatformHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(
    val promptRepository: PromptRepository,
    val userPreferencesRepository: UserPreferencesRepository,
    val promptSyncStore: PromptSyncStore,
    val secureCredentialStore: SecureCredentialStore,
)

fun createAppContainer(
    databaseBuilder: RoomDatabase.Builder<PromptDatabase>,
    preferencesDataStore: DataStore<Preferences>,
    secureCredentialStore: SecureCredentialStore,
    appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
): AppContainer {
    val database = buildPromptDatabase(databaseBuilder)
    val userPreferencesRepository = UserPreferencesRepository(preferencesDataStore)
    val syncCoordinator = PromptSyncCoordinator(
        localStore = RoomPromptSyncLocalStore(database),
        userPreferencesRepository = userPreferencesRepository,
        secureCredentialStore = secureCredentialStore,
        remotes = listOf(
            DropboxPromptSyncRemote(createPlatformHttpClient()),
        ),
    )
    val promptRepository = SyncingPromptRepository(
        delegate = RoomPromptRepository(
            promptDao = database.promptDao(),
            deviceIdProvider = userPreferencesRepository::getOrCreateDeviceId,
        ),
        syncStore = syncCoordinator,
        appScope = appScope,
    )

    return AppContainer(
        promptRepository = promptRepository,
        userPreferencesRepository = userPreferencesRepository,
        promptSyncStore = syncCoordinator,
        secureCredentialStore = secureCredentialStore,
    )
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided.")
}
