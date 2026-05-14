package com.mahdimalv.promptstash.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mahdimalv.promptstash.PromptStashApp
import com.mahdimalv.promptstash.createAppContainer

fun main() = application {
    val appContainer = remember { createAppContainer() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "PromptStash",
        icon = painterResource("icon.png"),
        state = rememberWindowState(width = 400.dp, height = 840.dp),
    ) {
        PromptStashApp(appContainer = appContainer)
    }
}
