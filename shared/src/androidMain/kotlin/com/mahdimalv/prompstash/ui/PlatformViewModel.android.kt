package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass

@Composable
actual fun <VM : ViewModel> platformViewModel(
    modelClass: KClass<VM>,
    factory: () -> VM,
): VM = viewModel(
    modelClass = modelClass,
    factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = factory() as T
    },
)
