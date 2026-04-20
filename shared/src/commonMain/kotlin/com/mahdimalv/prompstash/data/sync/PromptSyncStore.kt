package com.mahdimalv.prompstash.data.sync

import kotlinx.coroutines.flow.StateFlow

interface PromptSyncStore {
    val isSyncing: StateFlow<Boolean>

    suspend fun sync(trigger: SyncTrigger): SyncResult
}
