package com.mahdimalv.prompstash.data.sync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class DropboxAuthManager(
    private val secureCredentialStore: SecureCredentialStore,
    private val httpClient: HttpClient,
    private val browserLauncher: ExternalUrlLauncher,
    private val redirectHandler: DropboxAuthorizationRedirectHandler,
    private val appScope: CoroutineScope,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val randomBytes: (Int) -> ByteArray = { size -> ByteArray(size).also(Random::nextBytes) },
) {
    private val authMutex = Mutex()
    private val _authState = MutableStateFlow(DropboxAuthState())
    private val _events = MutableSharedFlow<DropboxAuthEvent>()

    private var authorizationJob: Job? = null

    val authState: StateFlow<DropboxAuthState> = _authState.asStateFlow()
    val events: SharedFlow<DropboxAuthEvent> = _events.asSharedFlow()

    init {
        appScope.launch {
            refreshState()
        }
    }

    suspend fun startAuthorization(): String = authMutex.withLock {
        if (DropboxBuildConfig.CLIENT_ID.isBlank()) {
            throw DropboxAuthException(
                "Dropbox auth is not configured for this build. Add dropbox.app.key to local.properties."
            )
        }
        if (authorizationJob?.isActive == true) {
            return "Dropbox auth is already in progress."
        }

        val redirect = redirectHandler.prepareAuthorizationRedirect()
        val state = randomUrlSafeToken(32)
        val codeVerifier = randomUrlSafeToken(64)
        val authUrl = buildAuthorizationUrl(
            clientId = DropboxBuildConfig.CLIENT_ID,
            redirectUri = redirect.redirectUri,
            state = state,
            codeChallenge = codeChallengeFor(codeVerifier),
        )

        if (!browserLauncher.openUrl(authUrl)) {
            redirect.close()
            _authState.value = _authState.value.copy(
                isAuthorizing = false,
                lastErrorMessage = "Could not open a browser for Dropbox auth."
            )
            throw DropboxAuthException("Could not open a browser for Dropbox auth.")
        }

        _authState.value = _authState.value.copy(
            isAuthorizing = true,
            lastErrorMessage = null,
        )
        authorizationJob = appScope.launch {
            completeAuthorization(
                pendingRedirect = redirect,
                expectedState = state,
                codeVerifier = codeVerifier,
            )
        }
        "Continue Dropbox auth in your browser."
    }

    fun receiveRedirectUri(uri: String) {
        redirectHandler.receiveRedirectUri(uri)
    }

    suspend fun removeAuthentication() {
        clearAuthentication()
        _events.emit(DropboxAuthEvent.Message("Dropbox auth removed."))
    }

    suspend fun clearAuthentication(
        message: String? = null,
        emitMessage: Boolean = false,
    ) {
        authMutex.withLock {
            authorizationJob?.cancel()
            authorizationJob = null
            secureCredentialStore.clearDropboxSession()
            _authState.value = DropboxAuthState(lastErrorMessage = message)
        }
        if (emitMessage && message != null) {
            _events.emit(DropboxAuthEvent.Message(message))
        }
    }

    suspend fun getValidAccessToken(): String? {
        val session = secureCredentialStore.readDropboxSession() ?: return null
        if (!session.isAccessTokenExpired(clock())) {
            return session.accessToken
        }

        return try {
            val refreshedSession = refreshAccessToken(session)
            secureCredentialStore.saveDropboxSession(refreshedSession)
            _authState.value = _authState.value.copy(
                isAuthenticated = true,
                isAuthorizing = false,
                accountLabel = refreshedSession.accountLabel,
                lastErrorMessage = null,
            )
            refreshedSession.accessToken
        } catch (error: DropboxAuthException) {
            if (error.shouldClearSession) {
                clearAuthentication(message = error.message)
            }
            throw error
        }
    }

    private suspend fun completeAuthorization(
        pendingRedirect: PendingDropboxAuthorizationRedirect,
        expectedState: String,
        codeVerifier: String,
    ) {
        try {
            val redirectUri = pendingRedirect.awaitRedirectUri()
            val callback = Url(redirectUri)
            val returnedState = callback.parameters["state"]
            val error = callback.parameters["error"]
            val errorDescription = callback.parameters["error_description"]
            val code = callback.parameters["code"]

            when {
                error != null -> {
                    handleAuthorizationFailure(
                        errorDescription?.takeIf(String::isNotBlank)
                            ?: "Dropbox auth was cancelled."
                    )
                }

                code.isNullOrBlank() -> {
                    handleAuthorizationFailure("Dropbox auth did not return an authorization code.")
                }

                returnedState != expectedState -> {
                    handleAuthorizationFailure("Dropbox auth response did not match the current request.")
                }

                else -> {
                    val exchangedSession = exchangeAuthorizationCode(
                        code = code,
                        codeVerifier = codeVerifier,
                        redirectUri = pendingRedirect.redirectUri,
                    )
                    secureCredentialStore.saveDropboxSession(exchangedSession)
                    _authState.value = DropboxAuthState(
                        isAuthenticated = true,
                        isAuthorizing = false,
                        accountLabel = exchangedSession.accountLabel,
                    )
                    _events.emit(DropboxAuthEvent.Authenticated(exchangedSession.accountLabel))
                    _events.emit(
                        DropboxAuthEvent.Message(
                            exchangedSession.accountLabel?.let { "Dropbox connected to $it." }
                                ?: "Dropbox connected."
                        )
                    )
                }
            }
        } catch (error: DropboxAuthException) {
            handleAuthorizationFailure(error.message ?: "Dropbox auth failed.")
        } catch (_: Exception) {
            handleAuthorizationFailure("Dropbox auth failed.")
        } finally {
            pendingRedirect.close()
            authorizationJob = null
        }
    }

    private suspend fun handleAuthorizationFailure(message: String) {
        refreshState(lastErrorMessage = message, preserveAuth = true)
        _events.emit(DropboxAuthEvent.Message(message))
    }

    private suspend fun exchangeAuthorizationCode(
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): DropboxAuthSession {
        val response = httpClient.post(DROPBOX_TOKEN_URL) {
            setBody(
                FormDataContent(
                    parameters {
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("client_id", DropboxBuildConfig.CLIENT_ID)
                        append("redirect_uri", redirectUri)
                        append("code_verifier", codeVerifier)
                    }
                )
            )
        }

        val responseBody = response.body<String>()
        if (!response.status.isSuccess()) {
            throw parseTokenError(responseBody)
        }

        val tokenResponse = json.decodeFromString(DropboxTokenResponse.serializer(), responseBody)
        val accountLabel = fetchAccountLabel(tokenResponse.accessToken)
        return DropboxAuthSession(
            refreshToken = tokenResponse.refreshToken
                ?: throw DropboxAuthException("Dropbox did not return a refresh token."),
            accessToken = tokenResponse.accessToken,
            accessTokenExpiresAt = tokenResponse.expiresIn?.let { clock() + (it * 1_000L) },
            accountLabel = accountLabel,
        )
    }

    private suspend fun refreshAccessToken(session: DropboxAuthSession): DropboxAuthSession {
        val response = httpClient.post(DROPBOX_TOKEN_URL) {
            setBody(
                FormDataContent(
                    parameters {
                        append("refresh_token", session.refreshToken)
                        append("grant_type", "refresh_token")
                        append("client_id", DropboxBuildConfig.CLIENT_ID)
                    }
                )
            )
        }

        val responseBody = response.body<String>()
        if (!response.status.isSuccess()) {
            throw parseTokenError(responseBody)
        }

        val tokenResponse = json.decodeFromString(DropboxRefreshResponse.serializer(), responseBody)
        return session.copy(
            accessToken = tokenResponse.accessToken,
            accessTokenExpiresAt = tokenResponse.expiresIn?.let { clock() + (it * 1_000L) },
        )
    }

    private suspend fun fetchAccountLabel(accessToken: String): String? {
        val response = httpClient.post(DROPBOX_CURRENT_ACCOUNT_URL) {
            headers.append("Authorization", "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) return null

        val body = response.body<String>()
        val account = json.decodeFromString(DropboxCurrentAccountResponse.serializer(), body)
        return account.name.displayName.ifBlank { account.email.orEmpty() }.ifBlank { null }
    }

    private fun parseTokenError(responseBody: String): DropboxAuthException {
        val error = runCatching {
            json.decodeFromString(DropboxTokenErrorResponse.serializer(), responseBody)
        }.getOrNull()

        return when (error?.error) {
            "invalid_grant" -> DropboxAuthException(
                "Dropbox authentication expired. Authenticate again in Settings.",
                shouldClearSession = true,
            )

            else -> DropboxAuthException(
                error?.errorDescription
                    ?: "Dropbox auth failed. Please try again."
            )
        }
    }

    private suspend fun refreshState(
        lastErrorMessage: String? = null,
        preserveAuth: Boolean = false,
    ) {
        val session = secureCredentialStore.readDropboxSession()
        _authState.value = DropboxAuthState(
            isAuthenticated = preserveAuth && _authState.value.isAuthenticated || session != null,
            isAuthorizing = false,
            accountLabel = session?.accountLabel,
            lastErrorMessage = lastErrorMessage,
        )
    }

    private fun buildAuthorizationUrl(
        clientId: String,
        redirectUri: String,
        state: String,
        codeChallenge: String,
    ): String = buildString {
        append("https://www.dropbox.com/oauth2/authorize")
        append("?response_type=code")
        append("&client_id=").append(clientId.encodeURLParameter())
        append("&redirect_uri=").append(redirectUri.encodeURLParameter())
        append("&token_access_type=offline")
        append("&code_challenge_method=S256")
        append("&code_challenge=").append(codeChallenge.encodeURLParameter())
        append("&state=").append(state.encodeURLParameter())
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun randomUrlSafeToken(byteCount: Int): String =
        Base64.UrlSafe.encode(randomBytes(byteCount)).trimEnd('=')

    @OptIn(ExperimentalEncodingApi::class)
    private fun codeChallengeFor(codeVerifier: String): String =
        Base64.UrlSafe.encode(sha256(codeVerifier.encodeToByteArray())).trimEnd('=')

    private fun DropboxAuthSession.isAccessTokenExpired(now: Long): Boolean {
        val expiresAt = accessTokenExpiresAt ?: return false
        return now >= expiresAt - ACCESS_TOKEN_EXPIRY_SKEW_MS
    }

    private companion object {
        private const val ACCESS_TOKEN_EXPIRY_SKEW_MS = 60_000L
        private const val DROPBOX_CURRENT_ACCOUNT_URL = "https://api.dropboxapi.com/2/users/get_current_account"
        private const val DROPBOX_TOKEN_URL = "https://api.dropboxapi.com/oauth2/token"
    }
}

@Serializable
private data class DropboxTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
)

@Serializable
private data class DropboxRefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long? = null,
)

@Serializable
private data class DropboxTokenErrorResponse(
    val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
)

@Serializable
private data class DropboxCurrentAccountResponse(
    val name: DropboxAccountName,
    val email: String? = null,
)

@Serializable
private data class DropboxAccountName(
    @SerialName("display_name") val displayName: String,
)
