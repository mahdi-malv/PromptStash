package com.mahdimalv.prompstash.ui

import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.PromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestPromptRepository(
    initialPrompts: List<Prompt> = emptyList(),
) : PromptRepository {
    private val prompts = MutableStateFlow(initialPrompts.sortedByDescending(Prompt::updatedAt))

    override fun observePrompts(): Flow<List<Prompt>> = prompts

    override fun observePrompt(id: String): Flow<Prompt?> = prompts.map { items ->
        items.firstOrNull { it.id == id }
    }

    override suspend fun upsertPrompt(prompt: Prompt) {
        val mutablePrompts = prompts.value.toMutableList()
        val index = mutablePrompts.indexOfFirst { it.id == prompt.id }
        if (index >= 0) {
            mutablePrompts[index] = prompt
        } else {
            mutablePrompts.add(prompt)
        }
        prompts.value = mutablePrompts.sortedByDescending(Prompt::updatedAt)
    }

    override suspend fun deletePrompt(id: String) {
        prompts.value = prompts.value.filterNot { it.id == id }
    }
}
