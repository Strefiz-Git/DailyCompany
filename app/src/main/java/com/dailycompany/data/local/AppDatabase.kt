package com.dailycompany.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailycompany.data.local.event.EventDao
import com.dailycompany.data.local.event.EventEntity
import com.dailycompany.data.local.event.EventExceptionDao
import com.dailycompany.data.local.event.EventExceptionEntity
import com.dailycompany.data.local.task.TaskConverters
import com.dailycompany.data.local.task.TaskDao
import com.dailycompany.data.local.task.TaskEntity

/**
 * Base de dados Room da app — tabelas `tasks`, `events` e `event_exceptions`.
 * O schema é exportado para `app/schemas` (regra: nunca commitar por cima,
 * versionar os `1.json`, `2.json`, `3.json` e agora `4.json`).
 *
 * ```json
 * entities = [TaskEntity, EventEntity, EventExceptionEntity]
 * version  = 4
 * ```
 *
 * Regra do projeto **R6 (inviolável)**: a ocorrência **nunca é gravada** — só
 * exceções pontuais em `event_exceptions`. A repetição vive numa única coluna
 * `recurrence` serializada (convenção `TaskEntity.repeat`/`TaskIcon.serialize`).
 */
@Database(
    entities = [TaskEntity::class, EventEntity::class, EventExceptionEntity::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(TaskConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao
    abstract fun eventExceptionDao(): EventExceptionDao

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

        /**
         * v3 → v4: cria as tabelas `events` e `event_exceptions` (Passo 2 da
         * Fase 2 — Eventos). Nenhuma ocorrência é gravada (R6); a repetição é
         * uma única coluna serializada `recurrence`.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `uid` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `time` TEXT,
                        `is_all_day` INTEGER NOT NULL,
                        `location` TEXT,
                        `notes` TEXT,
                        `color_key` TEXT,
                        `recurrence` TEXT NOT NULL DEFAULT 'NONE|1|||NEVER',
                        `created_at` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `event_exceptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `event_uid` TEXT NOT NULL,
                        `original_date` TEXT NOT NULL,
                        `is_cancelled` INTEGER NOT NULL,
                        `override_date` TEXT,
                        `override_time` TEXT
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
