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

/**
 * Módulo Hilt global (faixa de baixo "Companhia" ≈ falha sem engano
 * depende de um @Module com o objeto AppDatabase).
 *
 * Nota AGENTS: nenhum outro ficheiro chama `Room.databaseBuilder` — só aqui.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    @Provides
    @Singleton
    fun provideAppClock(): AppClock = SystemClock()
}
