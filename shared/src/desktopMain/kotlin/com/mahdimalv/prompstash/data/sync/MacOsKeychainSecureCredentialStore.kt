package com.mahdimalv.prompstash.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val KEYCHAIN_SERVICE = "PrompStash"

class MacOsKeychainSecureCredentialStore(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : SecureCredentialStore {

    override suspend fun readDropboxSession(): DropboxAuthSession? = withContext(Dispatchers.IO) {
        val result = securityCommand(
            "find-generic-password",
            "-a",
            DROPBOX_ACCOUNT_NAME,
            "-s",
            KEYCHAIN_SERVICE,
            "-w",
        )
        if (result.exitCode != 0) return@withContext null
        result.stdout.trim().ifBlank { null }?.let {
            json.decodeFromString(DropboxAuthSession.serializer(), it)
        }
    }

    override suspend fun saveDropboxSession(session: DropboxAuthSession) = withContext(Dispatchers.IO) {
        val result = securityCommand(
            "add-generic-password",
            "-U",
            "-a",
            DROPBOX_ACCOUNT_NAME,
            "-s",
            KEYCHAIN_SERVICE,
            "-w",
            json.encodeToString(DropboxAuthSession.serializer(), session),
        )
        if (result.exitCode != 0) {
            error("Unable to save the Dropbox auth session in macOS Keychain.")
        }
    }

    override suspend fun clearDropboxSession() = withContext(Dispatchers.IO) {
        securityCommand(
            "delete-generic-password",
            "-a",
            DROPBOX_ACCOUNT_NAME,
            "-s",
            KEYCHAIN_SERVICE,
        )
        Unit
    }
}

private const val DROPBOX_ACCOUNT_NAME = "remote-dropbox"

private data class SecurityCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

private fun securityCommand(vararg args: String): SecurityCommandResult {
    val process = ProcessBuilder(listOf("security", *args))
        .redirectErrorStream(false)
        .start()

    val stdout = process.inputStream.bufferedReader().use { it.readText() }
    val stderr = process.errorStream.bufferedReader().use { it.readText() }
    val exitCode = process.waitFor()
    return SecurityCommandResult(exitCode, stdout, stderr)
}
