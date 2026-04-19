package com.mahdimalv.prompstash.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mahdimalv.prompstash.PrompStashApp
import com.mahdimalv.prompstash.createAppContainer

fun main() = application {
    val appContainer = remember { createAppContainer() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "PrompStash",
        state = rememberWindowState(width = 400.dp, height = 840.dp),
    ) {
        PrompStashApp(appContainer = appContainer)
    }
}
