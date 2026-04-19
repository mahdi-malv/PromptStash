package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel

@Composable
actual fun <VM : ViewModel> platformViewModel(factory: () -> VM): VM = remember { factory() }
