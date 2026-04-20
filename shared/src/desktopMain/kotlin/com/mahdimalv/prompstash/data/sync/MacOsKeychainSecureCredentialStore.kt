package com.mahdimalv.prompstash.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEYCHAIN_SERVICE = "PrompStash"

class MacOsKeychainSecureCredentialStore : SecureCredentialStore {

    override suspend fun readAccessToken(remoteType: RemoteType): String? = withContext(Dispatchers.IO) {
        val result = securityCommand(
            "find-generic-password",
            "-a",
            remoteType.accountName(),
            "-s",
            KEYCHAIN_SERVICE,
            "-w",
        )
        if (result.exitCode != 0) return@withContext null
        result.stdout.trim().ifBlank { null }
    }

    override suspend fun saveAccessToken(remoteType: RemoteType, accessToken: String) = withContext(Dispatchers.IO) {
        val result = securityCommand(
            "add-generic-password",
            "-U",
            "-a",
            remoteType.accountName(),
            "-s",
            KEYCHAIN_SERVICE,
            "-w",
            accessToken,
        )
        if (result.exitCode != 0) {
            error("Unable to save the Dropbox access token in macOS Keychain.")
        }
    }

    override suspend fun clearAccessToken(remoteType: RemoteType) = withContext(Dispatchers.IO) {
        securityCommand(
            "delete-generic-password",
            "-a",
            remoteType.accountName(),
            "-s",
            KEYCHAIN_SERVICE,
        )
        Unit
    }

    private fun RemoteType.accountName(): String = "remote-${name.lowercase()}"
}

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
