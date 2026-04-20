package com.mahdimalv.prompstash.data.sync

object PromptSyncMerger {

    fun merge(
        localRecords: List<PromptSyncRecord>,
        remoteRecords: List<PromptSyncRecord>,
    ): List<PromptSyncRecord> {
        val localById = localRecords.associateBy(PromptSyncRecord::id)
        val remoteById = remoteRecords.associateBy(PromptSyncRecord::id)

        return (localById.keys + remoteById.keys).map { recordId ->
            val local = localById[recordId]
            val remote = remoteById[recordId]
            when {
                local == null -> requireNotNull(remote)
                remote == null -> local
                local.modifiedAt != remote.modifiedAt -> {
                    if (local.modifiedAt >= remote.modifiedAt) local else remote
                }

                local.modifiedByDeviceId != remote.modifiedByDeviceId -> {
                    if (local.modifiedByDeviceId >= remote.modifiedByDeviceId) local else remote
                }

                local.updatedAt != remote.updatedAt -> {
                    if (local.updatedAt >= remote.updatedAt) local else remote
                }

                else -> local
            }
        }.sortedBy(PromptSyncRecord::id)
    }
}
