package com.mahdimalv.promptstash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mahdimalv.promptstash.PromptStashApp
import com.mahdimalv.promptstash.widget.PromptStashWidgetUpdater
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy(LazyThreadSafetyMode.NONE) {
        createAppContainer(
            context = applicationContext,
            onPromptsChanged = { PromptStashWidgetUpdater.update(applicationContext) },
            onPinnedPromptsChanged = { PromptStashWidgetUpdater.update(applicationContext) },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDropboxRedirect(intent)
        enableEdgeToEdge()
        setContent {
            PromptStashApp(appContainer = appContainer)
        }
        observePinnedPromptChanges()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDropboxRedirect(intent)
    }

    private fun handleDropboxRedirect(intent: Intent?) {
        intent?.dataString?.takeIf { it.startsWith("promptstash://dropbox/auth") }?.let {
            appContainer.dropboxAuthManager.receiveRedirectUri(it)
        }
    }

    private fun observePinnedPromptChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.userPreferencesRepository.pinnedPromptIds.collect {
                    PromptStashWidgetUpdater.update(applicationContext)
                }
            }
        }
    }
}
