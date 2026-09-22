package com.dailycompany.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailycompany.data.local.task.TaskConverters
import com.dailycompany.data.local.task.TaskDao
import com.dailycompany.data.local.task.TaskEntity

/**
 * Base de dados Room da app — hoje com a única tabela `tasks`.
 * O schema é exportado para `app/schemas` (regra: nunca commitar por cima,
 * versionar os `1.json`, `2.json` e `3.json`).
 */
@Database(
    entities = [TaskEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(TaskConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        /** v1 → v2: adiciona a coluna `icon` (ícone/emoji da tarefa). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN icon TEXT")
            }
        }

        /** v2 → v3: adiciona a coluna `repeat` (repetição da tarefa). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN repeat TEXT NOT NULL DEFAULT 'NONE'")
            }
        }
    }
}
