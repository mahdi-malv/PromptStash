package com.mahdimalv.prompstash.sync

import com.mahdimalv.prompstash.data.sync.PromptSyncMerger
import com.mahdimalv.prompstash.data.sync.PromptSyncRecord
import kotlin.test.Test
import kotlin.test.assertEquals

class PromptSyncMergerTest {

    @Test
    fun remoteNewerEditWins() {
        val merged = PromptSyncMerger.merge(
            localRecords = listOf(record(id = "prompt", modifiedAt = 10, body = "local")),
            remoteRecords = listOf(record(id = "prompt", modifiedAt = 20, body = "remote")),
        )

        assertEquals("remote", merged.single().body)
    }

    @Test
    fun localNewerEditWins() {
        val merged = PromptSyncMerger.merge(
            localRecords = listOf(record(id = "prompt", modifiedAt = 30, body = "local")),
            remoteRecords = listOf(record(id = "prompt", modifiedAt = 20, body = "remote")),
        )

        assertEquals("local", merged.single().body)
    }

    @Test
    fun newerTombstoneWinsOverOlderLiveRecord() {
        val merged = PromptSyncMerger.merge(
            localRecords = listOf(record(id = "prompt", modifiedAt = 30, deletedAt = 30)),
            remoteRecords = listOf(record(id = "prompt", modifiedAt = 20, body = "remote")),
        )

        assertEquals(true, merged.single().isDeleted)
    }

    @Test
    fun newerLiveRecordRevivesOlderTombstone() {
        val merged = PromptSyncMerger.merge(
            localRecords = listOf(record(id = "prompt", modifiedAt = 20, deletedAt = 20)),
            remoteRecords = listOf(record(id = "prompt", modifiedAt = 30, body = "revived")),
        )

        assertEquals("revived", merged.single().body)
        assertEquals(false, merged.single().isDeleted)
    }

    @Test
    fun deviceIdBreaksModifiedAtTiesDeterministically() {
        val merged = PromptSyncMerger.merge(
            localRecords = listOf(record(id = "prompt", modifiedAt = 30, modifiedByDeviceId = "device-a")),
            remoteRecords = listOf(record(id = "prompt", modifiedAt = 30, modifiedByDeviceId = "device-z")),
        )

        assertEquals("device-z", merged.single().modifiedByDeviceId)
    }

    private fun record(
        id: String,
        modifiedAt: Long,
        body: String = "body",
        deletedAt: Long? = null,
        modifiedByDeviceId: String = "device-a",
    ): PromptSyncRecord = PromptSyncRecord(
        id = id,
        title = "title",
        body = body,
        tags = emptyList(),
        createdAt = 1,
        updatedAt = modifiedAt,
        deletedAt = deletedAt,
        modifiedAt = modifiedAt,
        modifiedByDeviceId = modifiedByDeviceId,
    )
}
