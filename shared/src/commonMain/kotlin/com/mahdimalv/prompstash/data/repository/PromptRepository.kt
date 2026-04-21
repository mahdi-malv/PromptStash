package com.mahdimalv.prompstash.data.repository

import com.mahdimalv.prompstash.data.local.PromptDao
import com.mahdimalv.prompstash.data.local.PromptEntity
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.sync.PromptSyncStore
import com.mahdimalv.prompstash.data.sync.SyncTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PromptRepository {
    fun observePrompts(): Flow<List<Prompt>>
    fun observePrompt(id: String): Flow<Prompt?>
    suspend fun upsertPrompt(prompt: Prompt)
    suspend fun deletePrompt(id: String)
}

class RoomPromptRepository(
    private val promptDao: PromptDao,
    private val deviceIdProvider: suspend () -> String,
    private val onPromptsChanged: suspend () -> Unit = {},
) : PromptRepository {

    override fun observePrompts(): Flow<List<Prompt>> = promptDao.observePrompts().map { prompts ->
        prompts.map(PromptEntity::toModel)
    }

    override fun observePrompt(id: String): Flow<Prompt?> = promptDao.observePrompt(id).map { prompt ->
        prompt?.toModel()
    }

    override suspend fun upsertPrompt(prompt: Prompt) {
        promptDao.upsertPrompt(prompt.toEntity(
            deletedAt = null,
            modifiedAt = prompt.updatedAt,
            modifiedByDeviceId = deviceIdProvider(),
        ))
        onPromptsChanged()
    }

    override suspend fun deletePrompt(id: String) {
        val existingPrompt = promptDao.getPromptById(id) ?: return
        val now = System.currentTimeMillis()
        promptDao.upsertPrompt(
            existingPrompt.copy(
                updatedAt = now,
                deletedAt = now,
                modifiedAt = now,
                modifiedByDeviceId = deviceIdProvider(),
            )
        )
        onPromptsChanged()
    }
}

class SyncingPromptRepository(
    private val delegate: PromptRepository,
    private val syncStore: PromptSyncStore,
    private val appScope: CoroutineScope,
    private val onPromptDeleted: suspend (String) -> Unit = {},
) : PromptRepository {

    override fun observePrompts(): Flow<List<Prompt>> = delegate.observePrompts()

    override fun observePrompt(id: String): Flow<Prompt?> = delegate.observePrompt(id)

    override suspend fun upsertPrompt(prompt: Prompt) {
        delegate.upsertPrompt(prompt)
        appScope.launch {
            syncStore.sync(SyncTrigger.AUTOMATIC)
        }
    }

    override suspend fun deletePrompt(id: String) {
        delegate.deletePrompt(id)
        onPromptDeleted(id)
        appScope.launch {
            syncStore.sync(SyncTrigger.AUTOMATIC)
        }
    }
}

private fun PromptEntity.toModel(): Prompt = Prompt(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Prompt.toEntity(
    deletedAt: Long?,
    modifiedAt: Long,
    modifiedByDeviceId: String,
): PromptEntity = PromptEntity(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    modifiedAt = modifiedAt,
    modifiedByDeviceId = modifiedByDeviceId,
)
