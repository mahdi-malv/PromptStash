package com.mahdimalv.prompstash.data.sync

interface ExternalUrlLauncher {
    fun openUrl(url: String): Boolean
}

interface DropboxAuthorizationRedirectHandler {
    suspend fun prepareAuthorizationRedirect(): PendingDropboxAuthorizationRedirect

    fun receiveRedirectUri(uri: String) = Unit
}

interface PendingDropboxAuthorizationRedirect {
    val redirectUri: String

    suspend fun awaitRedirectUri(): String

    fun close()
}
