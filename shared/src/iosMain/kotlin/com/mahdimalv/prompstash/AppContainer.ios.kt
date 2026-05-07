package com.mahdimalv.prompstash

import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabaseFileName
import com.mahdimalv.prompstash.data.settings.UserPreferencesFileName
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore
import com.mahdimalv.prompstash.data.sync.DropboxAuthException
import com.mahdimalv.prompstash.data.sync.DropboxAuthorizationRedirectHandler
import com.mahdimalv.prompstash.data.sync.DropboxAuthSession
import com.mahdimalv.prompstash.data.sync.ExternalUrlLauncher
import com.mahdimalv.prompstash.data.sync.PendingDropboxAuthorizationRedirect
import com.mahdimalv.prompstash.data.sync.SecureCredentialStore
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

private fun defaultAppDirectory(): String = "${NSHomeDirectory()}/Library/Application Support/PrompStash"

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
