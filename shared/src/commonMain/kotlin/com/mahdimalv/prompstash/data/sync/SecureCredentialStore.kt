package com.mahdimalv.prompstash.data.sync

interface SecureCredentialStore {
    suspend fun readAccessToken(remoteType: RemoteType): String?
    suspend fun saveAccessToken(remoteType: RemoteType, accessToken: String)
    suspend fun clearAccessToken(remoteType: RemoteType)
}
