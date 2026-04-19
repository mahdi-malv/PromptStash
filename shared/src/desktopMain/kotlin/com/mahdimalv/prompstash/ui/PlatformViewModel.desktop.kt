package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

@Composable
actual fun <VM : ViewModel> platformViewModel(
    modelClass: KClass<VM>,
    factory: () -> VM,
): VM = remember { factory() }
