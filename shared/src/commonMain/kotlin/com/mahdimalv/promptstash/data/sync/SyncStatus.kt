package com.mahdimalv.promptstash.data.sync

data class SyncStatus(
    val lastSyncAt: Long? = null,
    val lastSyncWasSuccessful: Boolean? = null,
    val lastSyncMessage: String? = null,
)
