package com.mahdimalv.prompstash

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val appContainer = remember { createAppContainer() }
    PrompStashApp(appContainer = appContainer)
}
