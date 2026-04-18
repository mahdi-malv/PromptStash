package com.mahdimalv.prompstash.data.repository

import com.mahdimalv.prompstash.data.local.PromptDao
import com.mahdimalv.prompstash.data.local.PromptEntity
import com.mahdimalv.prompstash.data.model.Prompt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PromptRepository {
    fun observePrompts(): Flow<List<Prompt>>
    fun observePrompt(id: String): Flow<Prompt?>
    suspend fun upsertPrompt(prompt: Prompt)
    suspend fun deletePrompt(id: String)
}

class RoomPromptRepository @Inject constructor(
    private val promptDao: PromptDao,
) : PromptRepository {

    override fun observePrompts(): Flow<List<Prompt>> = promptDao.observePrompts().map { prompts ->
        prompts.map(PromptEntity::toModel)
    }

    override fun observePrompt(id: String): Flow<Prompt?> = promptDao.observePrompt(id).map { prompt ->
        prompt?.toModel()
    }

    override suspend fun upsertPrompt(prompt: Prompt) {
        promptDao.upsertPrompt(prompt.toEntity())
    }

    override suspend fun deletePrompt(id: String) {
        promptDao.deletePrompt(id)
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

private fun Prompt.toEntity(): PromptEntity = PromptEntity(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
