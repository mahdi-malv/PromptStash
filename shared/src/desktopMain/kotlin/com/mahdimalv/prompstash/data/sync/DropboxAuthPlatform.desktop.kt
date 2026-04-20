package com.mahdimalv.prompstash.data.sync

import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred

private const val DESKTOP_DROPBOX_REDIRECT_PORT = 53682
private const val DESKTOP_DROPBOX_REDIRECT_PATH = "/dropbox/auth"

class DesktopExternalUrlLauncher : ExternalUrlLauncher {
    override fun openUrl(url: String): Boolean = runCatching {
        if (!Desktop.isDesktopSupported()) return false
        Desktop.getDesktop().browse(URI(url))
        true
    }.getOrDefault(false)
}

class DesktopDropboxAuthorizationRedirectHandler : DropboxAuthorizationRedirectHandler {
    override suspend fun prepareAuthorizationRedirect(): PendingDropboxAuthorizationRedirect {
        val redirectUri = "http://127.0.0.1:$DESKTOP_DROPBOX_REDIRECT_PORT$DESKTOP_DROPBOX_REDIRECT_PATH"
        val completion = CompletableDeferred<String>()
        val server = try {
            HttpServer.create(InetSocketAddress("127.0.0.1", DESKTOP_DROPBOX_REDIRECT_PORT), 0)
        } catch (_: Exception) {
            throw DropboxAuthException(
                "Desktop Dropbox auth could not start its local callback server on port $DESKTOP_DROPBOX_REDIRECT_PORT."
            )
        }

        server.createContext(DESKTOP_DROPBOX_REDIRECT_PATH) { exchange ->
            val requestUri = exchange.requestURI
            val fullUri = "$redirectUri${requestUri.rawQuery?.let { "?$it" }.orEmpty()}"
            if (!completion.isCompleted) {
                completion.complete(fullUri)
            }

            val response = if (requestUri.rawQuery?.contains("code=") == true) {
                "<html><body><h2>Dropbox connected</h2><p>You can return to PrompStash now.</p></body></html>"
            } else {
                "<html><body><h2>Dropbox auth incomplete</h2><p>You can return to PrompStash and try again.</p></body></html>"
            }
            val bytes = response.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()

        return object : PendingDropboxAuthorizationRedirect {
            override val redirectUri: String = redirectUri

            override suspend fun awaitRedirectUri(): String = completion.await()

            override fun close() {
                server.stop(0)
            }
        }
    }
}
