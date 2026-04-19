package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

@Composable
inline fun <reified VM : ViewModel> platformViewModel(noinline factory: () -> VM): VM =
    platformViewModel(VM::class, factory)

@Composable
expect fun <VM : ViewModel> platformViewModel(
    modelClass: KClass<VM>,
    factory: () -> VM,
): VM
