package com.dailycompany.data.repository

import com.dailycompany.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Fonte de tarefas vista pela camada de domínio/UI.
 * A UI nunca fala com `TaskDao` (regra AGENTS: dados ⇄ domínio via mapper).
 */
interface TaskRepository {
    /** Todas as tarefas (ativas + concluídas), por ordem de criação. */
    fun observeAll(): Flow<List<Task>>

    suspend fun getById(id: Long): Task?

    suspend fun save(task: Task): Long

    suspend fun setDone(id: Long, done: Boolean)

    suspend fun setDueDate(id: Long, date: java.time.LocalDate?)

    suspend fun deleteById(id: Long)
}
