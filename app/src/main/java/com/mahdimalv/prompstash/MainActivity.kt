package com.mahdimalv.prompstash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mahdimalv.prompstash.PrompStashApp

class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy(LazyThreadSafetyMode.NONE) {
        createAppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDropboxRedirect(intent)
        enableEdgeToEdge()
        setContent {
            PrompStashApp(appContainer = appContainer)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDropboxRedirect(intent)
    }

    private fun handleDropboxRedirect(intent: Intent?) {
        intent?.dataString?.takeIf { it.startsWith("prompstash://dropbox/auth") }?.let {
            appContainer.dropboxAuthManager.receiveRedirectUri(it)
        }
    }
}
