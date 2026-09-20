package com.dailycompany.data.local.task

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tarefa persistida (Room). Regra AGENTS: a UI nunca vê `TaskEntity` —
 * map e feito por [TaskMapper] para [com.dailycompany.domain.model.Task].
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val note: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriorityEntity = TaskPriorityEntity.NONE,
    val done: Boolean = false,
    val createdAt: Long = 0L,
)
