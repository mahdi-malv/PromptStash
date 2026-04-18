package com.mahdimalv.prompstash.di

import android.content.Context
import androidx.room.Room
import com.mahdimalv.prompstash.data.local.PromptDao
import com.mahdimalv.prompstash.data.local.PromptDatabase
import com.mahdimalv.prompstash.data.repository.PromptRepository
import com.mahdimalv.prompstash.data.repository.RoomPromptRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindPromptRepository(impl: RoomPromptRepository): PromptRepository
}

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun providePromptDatabase(@ApplicationContext context: Context): PromptDatabase {
        return Room.databaseBuilder(
            context,
            PromptDatabase::class.java,
            "prompstash.db",
        ).build()
    }

    @Provides
    fun providePromptDao(database: PromptDatabase): PromptDao = database.promptDao()
}
