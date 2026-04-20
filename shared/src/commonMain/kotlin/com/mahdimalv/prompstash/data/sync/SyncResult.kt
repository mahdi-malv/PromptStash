package com.mahdimalv.prompstash.data.sync

sealed interface SyncResult {
    val message: String

    data class Success(
        override val message: String,
        val syncedRecordCount: Int,
    ) : SyncResult

    data class Failure(
        override val message: String,
    ) : SyncResult

    data class Skipped(
        override val message: String,
    ) : SyncResult
}
