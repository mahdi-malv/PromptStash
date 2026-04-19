package com.mahdimalv.prompstash.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PromptTypeConverters {
    @TypeConverter
    fun fromTags(tags: List<String>): String = Json.encodeToString(tags)

    @TypeConverter
    fun toTags(serializedTags: String): List<String> = runCatching {
        Json.decodeFromString<List<String>>(serializedTags)
    }.getOrDefault(emptyList())
}
