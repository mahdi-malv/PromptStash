package com.mahdimalv.prompstash.sync

import com.mahdimalv.prompstash.data.sync.PromptSyncConflictException
import com.mahdimalv.prompstash.data.sync.PromptSyncCoordinator
import com.mahdimalv.prompstash.data.sync.PromptSyncLocalStore
import com.mahdimalv.prompstash.data.sync.PromptSyncRecord
import com.mahdimalv.prompstash.data.sync.PromptSyncRemote
import com.mahdimalv.prompstash.data.sync.RemoteSnapshot
import com.mahdimalv.prompstash.data.sync.RemoteType
import com.mahdimalv.prompstash.data.sync.SyncResult
import com.mahdimalv.prompstash.data.sync.SyncTrigger
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PromptSyncCoordinatorTest {

    @Test
    fun missingRemoteFileIsTreatedAsEmpty() = runTest {
        val userPreferences = createTestUserPreferencesRepository(backgroundScope)
        userPreferences.setRemoteType(RemoteType.DROPBOX)
        val credentials = InMemorySecureCredentialStore().apply {
            saveAccessToken(RemoteType.DROPBOX, "token")
        }
        val localStore = FakePromptSyncLocalStore(
            initialRecords = listOf(record(id = "local", modifiedAt = 10)),
        )
        val remote = FakePromptSyncRemote(
            downloads = ArrayDeque(listOf(RemoteSnapshot(records = emptyList(), revision = null))),
        )
        val coordinator = PromptSyncCoordinator(
            localStore = localStore,
            userPreferencesRepository = userPreferences,
            secureCredentialStore = credentials,
            remotes = listOf(remote),
            clock = { 100L },
        )

        val result = coordinator.sync(SyncTrigger.MANUAL)

        assertTrue(result is SyncResult.Success)
        assertEquals(1, remote.uploadAttempts.size)
        assertEquals(1, localStore.records.size)
    }

    @Test
    fun uploadConflictRetriesOnceWithFreshDownload() = runTest {
        val userPreferences = createTestUserPreferencesRepository(backgroundScope)
        userPreferences.setRemoteType(RemoteType.DROPBOX)
        val credentials = InMemorySecureCredentialStore().apply {
            saveAccessToken(RemoteType.DROPBOX, "token")
        }
        val localStore = FakePromptSyncLocalStore(
            initialRecords = listOf(record(id = "local", modifiedAt = 10)),
        )
        val remote = FakePromptSyncRemote(
            downloads = ArrayDeque(
                listOf(
                    RemoteSnapshot(records = emptyList(), revision = "rev-1"),
                    RemoteSnapshot(records = listOf(record(id = "remote", modifiedAt = 20)), revision = "rev-2"),
                )
            ),
            failUploadCount = 1,
        )
        val coordinator = PromptSyncCoordinator(
            localStore = localStore,
            userPreferencesRepository = userPreferences,
            secureCredentialStore = credentials,
            remotes = listOf(remote),
            clock = { 200L },
        )

        val result = coordinator.sync(SyncTrigger.MANUAL)

        assertTrue(result is SyncResult.Success)
        assertEquals(2, remote.uploadAttempts.size)
        assertEquals(2, remote.downloadCount)
        assertEquals(listOf("local", "remote"), localStore.records.map { it.id }.sorted())
    }

    private fun record(
        id: String,
        modifiedAt: Long,
    ): PromptSyncRecord = PromptSyncRecord(
        id = id,
        title = id,
        body = id,
        tags = emptyList(),
        createdAt = 1,
        updatedAt = modifiedAt,
        deletedAt = null,
        modifiedAt = modifiedAt,
        modifiedByDeviceId = "device-$id",
    )
}

private class FakePromptSyncLocalStore(
    initialRecords: List<PromptSyncRecord> = emptyList(),
) : PromptSyncLocalStore {
    var records: List<PromptSyncRecord> = initialRecords
        private set

    override suspend fun getAllRecords(): List<PromptSyncRecord> = records

    override suspend fun mergeRemoteRecords(remoteRecords: List<PromptSyncRecord>): List<PromptSyncRecord> {
        records = com.mahdimalv.prompstash.data.sync.PromptSyncMerger.merge(records, remoteRecords)
        return records
    }
}

private class FakePromptSyncRemote(
    val downloads: ArrayDeque<RemoteSnapshot>,
    private val failUploadCount: Int = 0,
) : PromptSyncRemote {
    override val remoteType: RemoteType = RemoteType.DROPBOX

    var downloadCount: Int = 0
        private set

    val uploadAttempts = mutableListOf<Pair<List<PromptSyncRecord>, String?>>()
    private var remainingUploadFailures: Int = failUploadCount

    override suspend fun download(accessToken: String): RemoteSnapshot {
        downloadCount += 1
        return downloads.removeFirstOrNull() ?: RemoteSnapshot(emptyList(), null)
    }

    override suspend fun upload(
        accessToken: String,
        records: List<PromptSyncRecord>,
        previousRevision: String?,
    ): String {
        uploadAttempts += records to previousRevision
        if (remainingUploadFailures > 0) {
            remainingUploadFailures -= 1
            throw PromptSyncConflictException()
        }
        return "new-rev"
    }
}
