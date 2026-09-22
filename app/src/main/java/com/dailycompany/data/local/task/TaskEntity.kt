package com.dailycompany.data.local.task

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tarefa persistida (Room). Regra AGENTS: a UI nunca vê `TaskEntity` — o
 * mapeamento é feito em [com.dailycompany.data.repository.RoomTaskRepository].
 * `dueDate`/`dueTime` guardam-se como ISO-8601 (converters); `created_at` e
 * `done_at` como epoch-millis (Long), alinhados com as queries do DAO.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val note: String = "",
    @ColumnInfo(name = "due_date") val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriorityEntity = TaskPriorityEntity.NONE,
    val done: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L,
    @ColumnInfo(name = "done_at") val doneAt: Long? = null,
)