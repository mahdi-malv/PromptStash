package com.mahdimalv.prompstash.data.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

private const val ANDROID_DROPBOX_REDIRECT_URI = "prompstash://dropbox/auth"

class AndroidExternalUrlLauncher(
    private val context: Context,
) : ExternalUrlLauncher {
    override fun openUrl(url: String): Boolean = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    }.getOrDefault(false)
}

class AndroidDropboxAuthorizationRedirectHandler : DropboxAuthorizationRedirectHandler {
    private val redirectUris = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override suspend fun prepareAuthorizationRedirect(): PendingDropboxAuthorizationRedirect =
        object : PendingDropboxAuthorizationRedirect {
            override val redirectUri: String = ANDROID_DROPBOX_REDIRECT_URI

            override suspend fun awaitRedirectUri(): String = redirectUris.first()

            override fun close() = Unit
        }

    override fun receiveRedirectUri(uri: String) {
        redirectUris.tryEmit(uri)
    }
}
