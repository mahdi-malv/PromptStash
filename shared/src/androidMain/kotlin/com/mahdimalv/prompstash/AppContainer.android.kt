package com.mahdimalv.prompstash

import android.content.Context
import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.sync.AndroidDropboxAuthorizationRedirectHandler
import com.mahdimalv.prompstash.data.sync.AndroidExternalUrlLauncher
import com.mahdimalv.prompstash.data.sync.AndroidSecureCredentialStore

fun createAppContainer(
    context: Context,
    onPromptsChanged: suspend () -> Unit = {},
    onPinnedPromptsChanged: suspend () -> Unit = {},
): AppContainer {
    val appContext = context.applicationContext
    existingAppContainer?.let { return it }

    return synchronized(appContainerLock) {
        existingAppContainer?.let { return@synchronized it }

        createAppContainer(
            databaseBuilder = Room.databaseBuilder<PromptDatabase>(
                context = appContext,
                name = appContext.getDatabasePath(com.mahdimalv.prompstash.data.local.PromptDatabaseFileName).absolutePath,
            ),
            preferencesDataStore = AndroidAppSingletons.preferencesDataStore(appContext),
            secureCredentialStore = AndroidSecureCredentialStore(appContext),
            externalUrlLauncher = AndroidExternalUrlLauncher(appContext),
            dropboxAuthorizationRedirectHandler = AndroidDropboxAuthorizationRedirectHandler(),
            onPromptsChanged = onPromptsChanged,
            onPinnedPromptsChanged = onPinnedPromptsChanged,
        ).also { container ->
            existingAppContainer = container
        }
    }
}

@Volatile
private var existingAppContainer: AppContainer? = null

private val appContainerLock = Any()
