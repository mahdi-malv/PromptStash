package com.mahdimalv.prompstash.data.sync

interface SecureCredentialStore {
    suspend fun readDropboxSession(): DropboxAuthSession?
    suspend fun saveDropboxSession(session: DropboxAuthSession)
    suspend fun clearDropboxSession()
}
