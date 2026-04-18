package com.mahdimalv.prompstash.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.model.Prompt
import com.mahdimalv.prompstash.data.repository.RoomPromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PromptRepositoryInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "prompt_repository_test.db"

    private lateinit var database: PromptDatabase
    private lateinit var repository: RoomPromptRepository

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
        database = Room.databaseBuilder(
            context,
            PromptDatabase::class.java,
            databaseName,
        ).build()
        repository = RoomPromptRepository(database.promptDao())
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun upsertUpdateDeleteRoundTrip() = runTest {
        val original = prompt(id = "1", title = "Original", body = "First body", updatedAt = 10L)
        val updated = original.copy(title = "Updated", body = "Updated body", updatedAt = 20L)

        repository.upsertPrompt(original)
        assertEquals("Original", repository.observePrompt("1").first()?.title)

        repository.upsertPrompt(updated)
        assertEquals("Updated", repository.observePrompt("1").first()?.title)

        repository.deletePrompt("1")
        assertTrue(repository.observePrompts().first().isEmpty())
    }

    @Test
    fun persistsAcrossDatabaseReopen() = runTest {
        repository.upsertPrompt(prompt(id = "persisted", title = "Persistent", body = "Keeps data", updatedAt = 30L))
        database.close()

        database = Room.databaseBuilder(
            context,
            PromptDatabase::class.java,
            databaseName,
        ).build()
        repository = RoomPromptRepository(database.promptDao())

        val restoredPrompt = repository.observePrompt("persisted").first()
        requireNotNull(restoredPrompt)
        assertEquals("Persistent", restoredPrompt.title)
        assertEquals("Keeps data", restoredPrompt.body)
    }

    private fun prompt(
        id: String,
        title: String,
        body: String,
        createdAt: Long = 1L,
        updatedAt: Long = createdAt,
    ): Prompt = Prompt(
        id = id,
        title = title,
        body = body,
        tags = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
