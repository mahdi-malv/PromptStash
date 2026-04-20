package com.mahdimalv.prompstash.data.sync

import android.content.Context
import android.util.Base64
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore

private const val SECURE_PREFS_FILE = "prompstash_secure_credentials"
private const val KEY_ALIAS = "prompstash_secure_token_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"

class AndroidSecureCredentialStore(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : SecureCredentialStore {

    override suspend fun readDropboxSession(): DropboxAuthSession? = withContext(Dispatchers.IO) {
        val storedValue = context.sharedPreferences().getString(DROPBOX_SESSION_KEY, null) ?: return@withContext null
        decrypt(storedValue)?.let { json.decodeFromString(DropboxAuthSession.serializer(), it) }
    }

    override suspend fun saveDropboxSession(session: DropboxAuthSession) = withContext(Dispatchers.IO) {
        context.sharedPreferences()
            .edit()
            .putString(DROPBOX_SESSION_KEY, encrypt(json.encodeToString(DropboxAuthSession.serializer(), session)))
            .apply()
    }

    override suspend fun clearDropboxSession() = withContext(Dispatchers.IO) {
        context.sharedPreferences()
            .edit()
            .remove(DROPBOX_SESSION_KEY)
            .apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encryptedBytes = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encodedCiphertext = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        return "$encodedIv:$encodedCiphertext"
    }

    private fun decrypt(storedValue: String): String? {
        val parts = storedValue.split(':', limit = 2)
        if (parts.size != 2) return null

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(128, iv),
        )
        return cipher.doFinal(ciphertext).toString(StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun Context.sharedPreferences() = getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE)

    private companion object {
        private const val DROPBOX_SESSION_KEY = "dropbox_session"
    }
}
