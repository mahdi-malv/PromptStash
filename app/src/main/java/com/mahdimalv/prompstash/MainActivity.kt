package com.mahdimalv.prompstash

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
        enableEdgeToEdge()
        setContent {
            PrompStashApp(appContainer = appContainer)
        }
    }
}
