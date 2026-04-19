package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel

@Composable
expect fun <VM : ViewModel> platformViewModel(factory: () -> VM): VM
