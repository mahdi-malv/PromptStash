package com.mahdimalv.prompstash

import android.content.Context
import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.PromptDatabaseFileName
import com.mahdimalv.prompstash.data.settings.UserPreferencesFileName
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore

fun createAppContainer(
    context: Context,
): AppContainer {
    val appContext = context.applicationContext
    val databaseFile = appContext.getDatabasePath(PromptDatabaseFileName)

    return createAppContainer(
        databaseBuilder = Room.databaseBuilder<PromptDatabase>(
            context = appContext,
            name = databaseFile.absolutePath,
        ),
        preferencesDataStore = createPreferencesDataStore(
            producePath = { appContext.filesDir.resolve(UserPreferencesFileName).absolutePath },
        ),
    )
}
