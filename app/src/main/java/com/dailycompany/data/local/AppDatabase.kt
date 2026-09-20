package com.dailycompany.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dailycompany.data.local.task.TaskConverters
import com.dailycompany.data.local.task.TaskDao
import com.dailycompany.data.local.task.TaskEntity

/**
 * Base de dados local (Room). Versão 1 = separador Tarefas completo.
 * `TaskConverters` é registado aqui (não dentro de `Room.databaseBuilder`).
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TaskConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
