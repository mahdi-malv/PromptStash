package com.mahdimalv.prompstash.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Library

@Serializable
data object QuickSave

@Serializable
data class Editor(
    val promptId: String? = null,
)

@Serializable
data object Settings
