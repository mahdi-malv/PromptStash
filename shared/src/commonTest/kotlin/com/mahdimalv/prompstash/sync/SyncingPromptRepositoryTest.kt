package com.mahdimalv.prompstash.sync

import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.SyncingPromptRepository
import com.mahdimalv.prompstash.data.sync.SyncTrigger
import com.mahdimalv.prompstash.ui.TestPromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SyncingPromptRepositoryTest {

    @Test
    fun autoSyncRunsAfterCreateAndDelete() = runTest {
        val delegate = TestPromptRepository()
        val syncStore = TestPromptSyncStore()
        val repository = SyncingPromptRepository(
            delegate = delegate,
            syncStore = syncStore,
            appScope = this,
        )

        repository.upsertPrompt(
            Prompt(
                id = "prompt",
                title = "Prompt",
                body = "Body",
                tags = emptyList(),
                createdAt = 1,
                updatedAt = 1,
            )
        )
        repository.deletePrompt("prompt")
        advanceUntilIdle()

        assertEquals(2, syncStore.syncCallCount)
        assertEquals(
            listOf(
                SyncTrigger.AUTOMATIC,
                SyncTrigger.AUTOMATIC,
            ),
            syncStore.triggers,
        )
    }
}
