package com.mahdimalv.prompstash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val modifiedAt: Long,
    val modifiedByDeviceId: String,
)
