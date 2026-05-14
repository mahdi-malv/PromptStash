package com.mahdimalv.promptstash

import androidx.room.Room
import com.mahdimalv.promptstash.data.local.PromptDatabase
import com.mahdimalv.promptstash.data.local.PromptDatabaseFileName
import com.mahdimalv.promptstash.data.settings.UserPreferencesFileName
import com.mahdimalv.promptstash.data.settings.createPreferencesDataStore
import com.mahdimalv.promptstash.data.sync.DropboxAuthException
import com.mahdimalv.promptstash.data.sync.DropboxAuthorizationRedirectHandler
import com.mahdimalv.promptstash.data.sync.DropboxAuthSession
import com.mahdimalv.promptstash.data.sync.ExternalUrlLauncher
import com.mahdimalv.promptstash.data.sync.PendingDropboxAuthorizationRedirect
import com.mahdimalv.promptstash.data.sync.SecureCredentialStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory

@OptIn(ExperimentalForeignApi::class)
fun createAppContainer(): AppContainer {
    val baseDirectory = defaultAppDirectory()
    NSFileManager.defaultManager.createDirectoryAtPath(baseDirectory, true, null, null)

    return createAppContainer(
        databaseBuilder = Room.databaseBuilder<PromptDatabase>(
            name = "$baseDirectory/$PromptDatabaseFileName",
        ),
        preferencesDataStore = createPreferencesDataStore(
            producePath = { "$baseDirectory/$UserPreferencesFileName" },
        ),
        secureCredentialStore = IosSecureCredentialStore(),
        externalUrlLauncher = IosExternalUrlLauncher(),
        dropboxAuthorizationRedirectHandler = IosDropboxAuthorizationRedirectHandler(),
        platformCapabilities = PlatformCapabilities(supportsRemoteSync = false),
    )
}

private fun defaultAppDirectory(): String = "${NSHomeDirectory()}/Library/Application Support/PromptStash"

private class IosSecureCredentialStore : SecureCredentialStore {
    override suspend fun readDropboxSession(): DropboxAuthSession? = null

    override suspend fun saveDropboxSession(session: DropboxAuthSession) = Unit

    override suspend fun clearDropboxSession() = Unit
}

private class IosExternalUrlLauncher : ExternalUrlLauncher {
    override fun openUrl(url: String): Boolean = false
}

private class IosDropboxAuthorizationRedirectHandler : DropboxAuthorizationRedirectHandler {
    override suspend fun prepareAuthorizationRedirect(): PendingDropboxAuthorizationRedirect {
        throw DropboxAuthException("Dropbox sync is not available on iOS yet.")
    }
}
