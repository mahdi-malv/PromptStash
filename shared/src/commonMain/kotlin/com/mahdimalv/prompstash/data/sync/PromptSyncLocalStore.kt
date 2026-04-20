package com.mahdimalv.prompstash.data.sync

import androidx.room.Transactor
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

    private val typeConverters = PromptTypeConverters()

    override suspend fun getAllRecords(): List<PromptSyncRecord> = withContext(Dispatchers.IO) {
        database.useConnection(isReadOnly = true) { connection ->
            connection.usePrepared(SELECT_ALL_PROMPTS_SQL) { statement ->
                statement.readPromptSyncRecords()
            }
        }
    }

    override suspend fun mergeRemoteRecords(remoteRecords: List<PromptSyncRecord>): List<PromptSyncRecord> =
        withContext(Dispatchers.IO) {
            database.useConnection(isReadOnly = false) { transactor ->
                transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    val localRecords = usePrepared(SELECT_ALL_PROMPTS_SQL) { statement ->
                        statement.readPromptSyncRecords()
                    }
                    val mergedRecords = PromptSyncMerger.merge(
                        localRecords = localRecords,
                        remoteRecords = remoteRecords,
                    )
                    usePrepared(DELETE_ALL_PROMPTS_SQL) { statement ->
                        statement.step()
                    }
                    mergedRecords.forEach { record ->
                        usePrepared(INSERT_PROMPT_SQL) { statement ->
                            statement.bindText(1, record.id)
                            statement.bindText(2, record.title)
                            statement.bindText(3, record.body)
                            statement.bindText(4, typeConverters.fromTags(record.tags))
                            statement.bindLong(5, record.createdAt)
                            statement.bindLong(6, record.updatedAt)
                            if (record.deletedAt == null) {
                                statement.bindNull(7)
                            } else {
                                statement.bindLong(7, record.deletedAt)
                            }
                            statement.bindLong(8, record.modifiedAt)
                            statement.bindText(9, record.modifiedByDeviceId)
                            statement.step()
                        }
                    }
                    mergedRecords
                }
            }
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
        private const val DELETE_ALL_PROMPTS_SQL = "DELETE FROM prompts"
        private const val INSERT_PROMPT_SQL = """
            INSERT INTO prompts (
                id,
                title,
                body,
                tags,
                createdAt,
                updatedAt,
                deletedAt,
                modifiedAt,
                modifiedByDeviceId
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
    }
}
