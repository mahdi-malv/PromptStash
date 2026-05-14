package com.mahdimalv.promptstash

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val appContainer = remember { createAppContainer() }
    PromptStashApp(appContainer = appContainer)
}
