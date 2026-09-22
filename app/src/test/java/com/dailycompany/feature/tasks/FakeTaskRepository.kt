package com.dailycompany.feature.tasks

import com.dailycompany.data.repository.TaskRepository
import com.dailycompany.domain.model.Task
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositório em memória para testes determinísticos (sem Room). As flatMapLatest
 * do repositório fake reemitem o estado corrente sempre que algo muda.
 */
class FakeTaskRepository(initial: List<Task> = emptyList()) : TaskRepository {

    private val _tasks = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeAll(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun getById(id: Long): Task? = _tasks.value.firstOrNull { it.id == id }

    override suspend fun save(task: Task): Long {
        val id = if (task.id == 0L) nextId++ else task.id
        _tasks.value = _tasks.value.filterNot { it.id == id } + task.copy(id = id)
        return id
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(done = done) else it
        }
    }

    override suspend fun setDueDate(id: Long, date: LocalDate?) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(dueDate = date) else it
        }
    }

    override suspend fun deleteById(id: Long) {
        _tasks.value = _tasks.value.filterNot { it.id == id }
    }
}
