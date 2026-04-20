package com.mahdimalv.prompstash

import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabaseFileName
import com.mahdimalv.prompstash.data.settings.UserPreferencesFileName
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore
import com.mahdimalv.prompstash.data.sync.DesktopDropboxAuthorizationRedirectHandler
import com.mahdimalv.prompstash.data.sync.DesktopExternalUrlLauncher
import com.mahdimalv.prompstash.data.sync.MacOsKeychainSecureCredentialStore
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlinx.coroutines.CoroutineScope
import kotlin.io.path.absolutePathString

fun createAppContainer(
    baseDirectory: Path = defaultAppDirectory(),
    preferencesScope: CoroutineScope? = null,
): AppContainer {
    Files.createDirectories(baseDirectory)
    val databasePath = baseDirectory.resolve(PromptDatabaseFileName)
    val preferencesPath = baseDirectory.resolve(UserPreferencesFileName)

    return createAppContainer(
        databaseBuilder = Room.databaseBuilder<PromptDatabase>(
            name = databasePath.absolutePathString(),
        ),
        preferencesDataStore = if (preferencesScope != null) {
            createPreferencesDataStore(
                producePath = { preferencesPath.absolutePathString() },
                scope = preferencesScope,
            )
        } else {
            createPreferencesDataStore(
                producePath = { preferencesPath.absolutePathString() },
            )
        },
        secureCredentialStore = MacOsKeychainSecureCredentialStore(),
        externalUrlLauncher = DesktopExternalUrlLauncher(),
        dropboxAuthorizationRedirectHandler = DesktopDropboxAuthorizationRedirectHandler(),
    )
}

private fun defaultAppDirectory(): Path = Paths.get(
    System.getProperty("user.home"),
    "Library",
    "Application Support",
    "PrompStash",
)
