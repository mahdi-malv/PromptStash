package com.mahdimalv.prompstash.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    fun observePrompt(id: String): Flow<PromptEntity?>

    @Upsert
    suspend fun upsertPrompt(prompt: PromptEntity)

    @Upsert
    suspend fun upsertPrompts(prompts: List<PromptEntity>)

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: String): PromptEntity?

    @Query("SELECT * FROM prompts")
    suspend fun getAllPromptEntities(): List<PromptEntity>

    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()
}
