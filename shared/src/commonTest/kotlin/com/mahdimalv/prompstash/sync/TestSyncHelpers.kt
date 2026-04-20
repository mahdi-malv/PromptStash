package com.mahdimalv.prompstash.sync

import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import com.mahdimalv.prompstash.data.settings.createPreferencesDataStore
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.RemoteSnapshot
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SecureCredentialStore
import com.mahdimalv.prompstash.data.sync.SyncResult
import com.mahdimalv.prompstash.data.sync.SyncTrigger
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestPromptSyncStore(
    initialResult: SyncResult = SyncResult.Success("Synced 0 prompts", 0),
) : PromptSyncStore {
    private val _isSyncing = MutableStateFlow(false)

    var syncCallCount: Int = 0
        private set

    var nextResult: SyncResult = initialResult

    val triggers = mutableListOf<SyncTrigger>()

    override val isSyncing: StateFlow<Boolean> = _isSyncing

    override suspend fun sync(trigger: SyncTrigger): SyncResult {
        syncCallCount += 1
        triggers += trigger
        return nextResult
    }

    fun setSyncing(value: Boolean) {
        _isSyncing.value = value
    }
}

class InMemorySecureCredentialStore : SecureCredentialStore {
    private val tokens = mutableMapOf<RemoteType, String>()

    override suspend fun readAccessToken(remoteType: RemoteType): String? = tokens[remoteType]

    override suspend fun saveAccessToken(remoteType: RemoteType, accessToken: String) {
        tokens[remoteType] = accessToken
    }

    override suspend fun clearAccessToken(remoteType: RemoteType) {
        tokens.remove(remoteType)
    }
}

suspend fun createTestUserPreferencesRepository(
    scope: CoroutineScope? = null,
): UserPreferencesRepository {
    val preferencesFile = Files.createTempFile("prompstash-test", ".preferences_pb")
    return UserPreferencesRepository(
        if (scope != null) {
            createPreferencesDataStore(
                producePath = { preferencesFile.toString() },
                scope = scope,
            )
        } else {
            createPreferencesDataStore(producePath = { preferencesFile.toString() })
        }
    )
}
