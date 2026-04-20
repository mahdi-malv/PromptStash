package com.mahdimalv.prompstash.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NavRoute : NavKey

@Serializable
data object Library : NavRoute

@Serializable
data class Editor(
    val promptId: String? = null,
) : NavRoute

@Serializable
data object Settings : NavRoute
