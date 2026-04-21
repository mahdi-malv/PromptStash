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
import com.mahdimalv.prompstash.data.sync.DropboxAuthManager
import com.mahdimalv.prompstash.data.sync.DropboxAuthorizationRedirectHandler
import com.mahdimalv.prompstash.data.sync.ExternalUrlLauncher
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
    val dropboxAuthManager: DropboxAuthManager,
)

fun createAppContainer(
    databaseBuilder: RoomDatabase.Builder<PromptDatabase>,
    preferencesDataStore: DataStore<Preferences>,
    secureCredentialStore: SecureCredentialStore,
    externalUrlLauncher: ExternalUrlLauncher,
    dropboxAuthorizationRedirectHandler: DropboxAuthorizationRedirectHandler,
    appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    onPromptsChanged: suspend () -> Unit = {},
    onPinnedPromptsChanged: suspend () -> Unit = {},
): AppContainer {
    val database = buildPromptDatabase(databaseBuilder)
    val userPreferencesRepository = UserPreferencesRepository(
        dataStore = preferencesDataStore,
        onPinnedPromptsChanged = onPinnedPromptsChanged,
    )
    val httpClient = createPlatformHttpClient()
    val dropboxAuthManager = DropboxAuthManager(
        secureCredentialStore = secureCredentialStore,
        httpClient = httpClient,
        browserLauncher = externalUrlLauncher,
        redirectHandler = dropboxAuthorizationRedirectHandler,
        appScope = appScope,
    )
    val syncCoordinator = PromptSyncCoordinator(
        localStore = RoomPromptSyncLocalStore(
            database = database,
            onPromptsChanged = onPromptsChanged,
        ),
        userPreferencesRepository = userPreferencesRepository,
        dropboxAuthManager = dropboxAuthManager,
        remotes = listOf(
            DropboxPromptSyncRemote(httpClient),
        ),
    )
    val promptRepository = SyncingPromptRepository(
        delegate = RoomPromptRepository(
            promptDao = database.promptDao(),
            deviceIdProvider = userPreferencesRepository::getOrCreateDeviceId,
            onPromptsChanged = onPromptsChanged,
        ),
        syncStore = syncCoordinator,
        appScope = appScope,
        onPromptDeleted = userPreferencesRepository::removePinnedPrompt,
    )

    return AppContainer(
        promptRepository = promptRepository,
        userPreferencesRepository = userPreferencesRepository,
        promptSyncStore = syncCoordinator,
        secureCredentialStore = secureCredentialStore,
        dropboxAuthManager = dropboxAuthManager,
    )
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided.")
}
