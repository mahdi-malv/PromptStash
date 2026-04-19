package com.mahdimalv.prompstash.data.model

data class Prompt(
    val id: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
) {
    val wordCount: Int
        get() = body.wordCount()
}

fun derivePromptTitle(body: String): String {
    val firstMeaningfulLine = body
        .lineSequence()
        .map(String::trim)
        .firstOrNull(String::isNotBlank)
        .orEmpty()

    return when {
        firstMeaningfulLine.isNotBlank() -> firstMeaningfulLine.take(60)
        else -> "Untitled prompt"
    }
}

fun String.wordCount(): Int {
    val normalized = trim()
    return if (normalized.isBlank()) 0 else normalized.split("\\s+".toRegex()).size
}
