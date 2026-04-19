package com.mahdimalv.prompstash

import com.mahdimalv.prompstash.data.settings.ThemePreference
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DesktopPersistenceTest {

    @Test
    fun promptAndThemePreferencePersistAcrossContainerRecreation() = runBlocking {
        val tempDirectory = Files.createTempDirectory("prompstash-desktop-test")
        val firstScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        val firstContainer = createAppContainer(
            baseDirectory = tempDirectory,
            preferencesScope = firstScope,
        )
        firstContainer.userPreferencesRepository.setThemePreference(ThemePreference.DARK)
        firstContainer.promptRepository.upsertPrompt(
            com.mahdimalv.prompstash.data.model.Prompt(
                id = "persisted",
                title = "Persistent prompt",
                body = "Keeps desktop data",
                tags = listOf("Desktop"),
                createdAt = 1L,
                updatedAt = 2L,
            )
        )
        firstScope.cancel()

        val secondScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val secondContainer = createAppContainer(
            baseDirectory = tempDirectory,
            preferencesScope = secondScope,
        )

        val restoredPrompt = secondContainer.promptRepository.observePrompt("persisted").first()
        assertNotNull(restoredPrompt)
        assertEquals("Persistent prompt", restoredPrompt.title)
        assertEquals("Keeps desktop data", restoredPrompt.body)
        assertEquals(ThemePreference.DARK, secondContainer.userPreferencesRepository.themePreference.first())
        secondScope.cancel()
    }
}
