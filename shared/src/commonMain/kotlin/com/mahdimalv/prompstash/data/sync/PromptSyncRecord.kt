package com.mahdimalv.prompstash.data.sync

import com.mahdimalv.prompstash.data.local.PromptEntity

data class PromptSyncRecord(
    val id: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val modifiedAt: Long,
    val modifiedByDeviceId: String,
) {
    val isDeleted: Boolean
        get() = deletedAt != null
}

internal fun PromptEntity.toSyncRecord(): PromptSyncRecord = PromptSyncRecord(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    modifiedAt = modifiedAt,
    modifiedByDeviceId = modifiedByDeviceId,
)

internal fun PromptSyncRecord.toEntity(): PromptEntity = PromptEntity(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    modifiedAt = modifiedAt,
    modifiedByDeviceId = modifiedByDeviceId,
)
