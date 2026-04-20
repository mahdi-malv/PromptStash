package com.mahdimalv.prompstash.data.local

import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

const val PromptDatabaseFileName = "prompstash.db"

fun buildPromptDatabase(
    builder: RoomDatabase.Builder<PromptDatabase>,
): PromptDatabase = builder
    .addMigrations(promptDatabaseMigration1To2())
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

private fun promptDatabaseMigration1To2(): Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.executeSql("ALTER TABLE prompts ADD COLUMN deletedAt INTEGER")
        connection.executeSql("ALTER TABLE prompts ADD COLUMN modifiedAt INTEGER NOT NULL DEFAULT 0")
        connection.executeSql("ALTER TABLE prompts ADD COLUMN modifiedByDeviceId TEXT NOT NULL DEFAULT ''")
        connection.executeSql("UPDATE prompts SET modifiedAt = updatedAt WHERE modifiedAt = 0")
    }
}

private fun SQLiteConnection.executeSql(sql: String) {
    prepare(sql).use { statement ->
        statement.step()
    }
}

@Suppress("KotlinNoActualForExpect")
expect object PromptDatabaseConstructor : RoomDatabaseConstructor<PromptDatabase> {
    override fun initialize(): PromptDatabase
}
