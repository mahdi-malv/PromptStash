package com.mahdimalv.prompstash

import android.content.Context
import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabaseFileName
import com.mahdimalv.prompstash.data.settings.UserPreferencesFileName
import com.mahdimalv.prompstash.data.sync.AndroidDropboxAuthorizationRedirectHandler
import com.mahdimalv.prompstash.data.sync.AndroidExternalUrlLauncher
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore
import com.mahdimalv.prompstash.data.sync.AndroidSecureCredentialStore

fun createAppContainer(
    context: Context,
): AppContainer {
    val appContext = context.applicationContext
    existingAppContainer?.let { return it }

    return synchronized(appContainerLock) {
        existingAppContainer?.let { return@synchronized it }

        val databaseFile = appContext.getDatabasePath(PromptDatabaseFileName)
        createAppContainer(
            databaseBuilder = Room.databaseBuilder<PromptDatabase>(
                context = appContext,
                name = databaseFile.absolutePath,
            ),
            preferencesDataStore = createPreferencesDataStore(
                producePath = { appContext.filesDir.resolve(UserPreferencesFileName).absolutePath },
            ),
            secureCredentialStore = AndroidSecureCredentialStore(appContext),
            externalUrlLauncher = AndroidExternalUrlLauncher(appContext),
            dropboxAuthorizationRedirectHandler = AndroidDropboxAuthorizationRedirectHandler(),
        ).also { container ->
            existingAppContainer = container
        }
    }
}

@Volatile
private var existingAppContainer: AppContainer? = null

private val appContainerLock = Any()
