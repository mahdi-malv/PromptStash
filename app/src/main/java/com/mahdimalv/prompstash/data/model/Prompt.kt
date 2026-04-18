package com.mahdimalv.prompstash.data.model

data class Prompt(
    val id: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val wordCount: Int = body.split("\\s+".toRegex()).size,
    val isPublic: Boolean = false,
)
