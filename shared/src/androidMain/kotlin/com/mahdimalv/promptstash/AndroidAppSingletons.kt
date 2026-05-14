package com.mahdimalv.promptstash

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.mahdimalv.promptstash.data.local.PromptDatabase
import com.mahdimalv.promptstash.data.local.PromptDatabaseFileName
import com.mahdimalv.promptstash.data.local.buildPromptDatabase
import com.mahdimalv.promptstash.data.settings.UserPreferencesFileName
import com.mahdimalv.promptstash.data.settings.createPreferencesDataStore

object AndroidAppSingletons {
    @Volatile
    private var promptDatabase: PromptDatabase? = null

    @Volatile
    private var preferencesDataStore: DataStore<Preferences>? = null

    fun promptDatabase(context: Context): PromptDatabase {
        promptDatabase?.let { return it }
        val appContext = context.applicationContext

        return synchronized(this) {
            promptDatabase ?: buildPromptDatabase(
                Room.databaseBuilder<PromptDatabase>(
                    context = appContext,
                    name = appContext.getDatabasePath(PromptDatabaseFileName).absolutePath,
                )
            ).also { database ->
                promptDatabase = database
            }
        }
    }

    fun preferencesDataStore(context: Context): DataStore<Preferences> {
        preferencesDataStore?.let { return it }
        val appContext = context.applicationContext

        return synchronized(this) {
            preferencesDataStore ?: createPreferencesDataStore(
                producePath = { appContext.filesDir.resolve(UserPreferencesFileName).absolutePath },
            ).also { dataStore ->
                preferencesDataStore = dataStore
            }
        }
    }
}
