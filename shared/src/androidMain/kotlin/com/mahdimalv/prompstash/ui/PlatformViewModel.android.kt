package com.mahdimalv.prompstash.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
actual fun <VM : ViewModel> platformViewModel(factory: () -> VM): VM = viewModel { factory() }
