package com.dailycompany.di

import com.dailycompany.data.repository.AlarmRepository
import com.dailycompany.data.repository.EventRepository
import com.dailycompany.data.repository.RoomAlarmRepository
import com.dailycompany.data.repository.RoomEventRepository
import com.dailycompany.data.repository.RoomTaskRepository
import com.dailycompany.data.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Liga as interfaces de domínio/repositório às implementações Room. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: RoomTaskRepository): TaskRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: RoomEventRepository): EventRepository

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: RoomAlarmRepository): AlarmRepository
}
