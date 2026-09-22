package com.dailycompany.di

import android.content.Context
import androidx.room.Room
import com.dailycompany.core.time.AppClock
import com.dailycompany.core.time.SystemClock
import com.dailycompany.data.local.AppDatabase
import com.dailycompany.data.local.task.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Módulo Hilt global — único ponto de fornecimento de Room, do relógio e
 * de dispatchers (regra AGENTS: nenhum outro ficheiro chama
 * `Room.databaseBuilder` nem `SystemClock()`).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppDiModule {

    // ---- Room ----
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dailycompany.db",
        ).build()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    // ---- Relógio (regra: injetado, nunca System.currentTimeMillis() na UI) ----
    @Provides
    @Singleton
    fun provideAppClock(): AppClock = SystemClock()

    // ---- Dispatchers ----
    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(2)
}
