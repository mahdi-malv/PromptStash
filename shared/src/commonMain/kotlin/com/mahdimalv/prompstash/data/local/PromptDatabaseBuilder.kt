package com.mahdimalv.prompstash.data.local

import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

const val PromptDatabaseFileName = "prompstash.db"

fun buildPromptDatabase(
    builder: RoomDatabase.Builder<PromptDatabase>,
): PromptDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

@Suppress("KotlinNoActualForExpect")
expect object PromptDatabaseConstructor : RoomDatabaseConstructor<PromptDatabase> {
    override fun initialize(): PromptDatabase
}
