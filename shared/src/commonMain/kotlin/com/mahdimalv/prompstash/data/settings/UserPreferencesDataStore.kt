package com.mahdimalv.prompstash.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.mahdimalv.prompstash.platformIoDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

const val UserPreferencesFileName = "user_preferences.preferences_pb"

fun createPreferencesDataStore(
    producePath: () -> String,
    scope: CoroutineScope = CoroutineScope(platformIoDispatcher() + SupervisorJob()),
): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    scope = scope,
    produceFile = { producePath().toPath() },
)
