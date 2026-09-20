package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Prioridade de uma tarefa (ordem = peso para ordenação). */
enum class TaskPriority {
    NONE,
    MEDIUM,
    HIGH,
}

/**
 * Tarefa — modelo de domínio puro, sem dependências Android/Room.
 * java.time nativo (minSdk 26 ≥ 26.0.1 ⇒ sem desugaring).
 */
data class Task(
    val id: Long = 0L,
    val title: String,
    val note: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: TaskPriority = TaskPriority.NONE,
    val done: Boolean = false,
    val createdAt: LocalDateTime,
    val doneAt: LocalDateTime? = null,
)

/** Tarefa criada a partir dos campos de "nova tarefa". */
fun newTask(
    title: String,
    note: String,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    priority: TaskPriority,
    createdAt: LocalDateTime,
): Task = Task(
    title = title,
    note = note,
    dueDate = dueDate,
    dueTime = dueTime,
    priority = priority,
    createdAt = createdAt,
)
