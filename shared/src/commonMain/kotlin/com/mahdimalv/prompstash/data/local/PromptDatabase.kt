package com.mahdimalv.prompstash.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PromptEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(PromptDatabaseConstructor::class)
@TypeConverters(PromptTypeConverters::class)
abstract class PromptDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
}
