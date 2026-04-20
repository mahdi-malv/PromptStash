package com.mahdimalv.prompstash.data.sync

data class RemoteSnapshot(
    val records: List<PromptSyncRecord>,
    val revision: String?,
)

interface PromptSyncRemote {
    val remoteType: RemoteType

    suspend fun download(accessToken: String): RemoteSnapshot

    suspend fun upload(
        accessToken: String,
        records: List<PromptSyncRecord>,
        previousRevision: String?,
    ): String
}

class PromptSyncConflictException(
    message: String = "The remote changed while syncing. Please retry.",
) : Exception(message)

class PromptSyncRemoteException(
    message: String,
) : Exception(message)
