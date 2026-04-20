package com.mahdimalv.prompstash.data.sync

import kotlinx.serialization.Serializable

@Serializable
data class DropboxAuthSession(
    val refreshToken: String,
    val accessToken: String,
    val accessTokenExpiresAt: Long?,
    val accountLabel: String? = null,
)

data class DropboxAuthState(
    val isAuthenticated: Boolean = false,
    val isAuthorizing: Boolean = false,
    val accountLabel: String? = null,
    val lastErrorMessage: String? = null,
)

sealed interface DropboxAuthEvent {
    data class Message(val value: String) : DropboxAuthEvent

    data class Authenticated(val accountLabel: String?) : DropboxAuthEvent
}

class DropboxAuthException(
    message: String,
    val shouldClearSession: Boolean = false,
) : Exception(message)
