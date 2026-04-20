package com.mahdimalv.prompstash.data.sync

import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.local.PromptTypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PromptSyncLocalStore {
    suspend fun getAllRecords(): List<PromptSyncRecord>
    suspend fun mergeRemoteRecords(remoteRecords: List<PromptSyncRecord>): List<PromptSyncRecord>
}

class RoomPromptSyncLocalStore(
    private val database: PromptDatabase,
) : PromptSyncLocalStore {
    private val promptDao = database.promptDao()
    private val typeConverters = PromptTypeConverters()

    override suspend fun getAllRecords(): List<PromptSyncRecord> = withContext(Dispatchers.IO) {
        promptDao.getAllPromptEntities().map { it.toSyncRecord() }
    }

    override suspend fun mergeRemoteRecords(remoteRecords: List<PromptSyncRecord>): List<PromptSyncRecord> =
        withContext(Dispatchers.IO) {
            val localRecords = database.useConnection(isReadOnly = true) { connection ->
                connection.usePrepared(SELECT_ALL_PROMPTS_SQL) { statement ->
                    statement.readPromptSyncRecords()
                }
            }
            val mergedRecords = PromptSyncMerger.merge(
                localRecords = localRecords,
                remoteRecords = remoteRecords,
            )
            promptDao.deleteAllPrompts()
            promptDao.upsertPrompts(mergedRecords.map { it.toEntity() })
            mergedRecords
        }

    private fun androidx.sqlite.SQLiteStatement.readPromptSyncRecords(): List<PromptSyncRecord> {
        val records = mutableListOf<PromptSyncRecord>()
        while (step()) {
            records += PromptSyncRecord(
                id = getText(0),
                title = getText(1),
                body = getText(2),
                tags = typeConverters.toTags(getText(3)),
                createdAt = getLong(4),
                updatedAt = getLong(5),
                deletedAt = if (isNull(6)) null else getLong(6),
                modifiedAt = getLong(7),
                modifiedByDeviceId = getText(8),
            )
        }
        return records
    }

    private companion object {
        private const val SELECT_ALL_PROMPTS_SQL =
            "SELECT id, title, body, tags, createdAt, updatedAt, deletedAt, modifiedAt, modifiedByDeviceId FROM prompts"
    }
}
