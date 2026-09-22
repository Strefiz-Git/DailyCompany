package com.dailycompany.data.repository

import com.dailycompany.core.time.AppClock
import com.dailycompany.data.local.task.TaskDao
import com.dailycompany.data.local.task.TaskEntity
import com.dailycompany.domain.model.Task
import com.dailycompany.domain.model.TaskPriority
import com.dailycompany.domain.model.parseTaskIcon
import com.dailycompany.domain.model.serialize
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementação Room de [TaskRepository].
 * `created_at`/`done_at` são guardados como epoch-millis (Long).
 */
@Singleton
class RoomTaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val clock: AppClock,
) : TaskRepository {

    private fun TaskEntity.toDomain(): Task = Task(
        id = id,
        title = title,
        note = note,
        dueDate = dueDate,
        dueTime = dueTime,
        priority = when (priority) {
            com.dailycompany.data.local.task.TaskPriorityEntity.NONE -> TaskPriority.NONE
            com.dailycompany.data.local.task.TaskPriorityEntity.MEDIUM -> TaskPriority.MEDIUM
            com.dailycompany.data.local.task.TaskPriorityEntity.HIGH -> TaskPriority.HIGH
        },
        icon = parseTaskIcon(icon),
        done = done,
        createdAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(createdAt),
            clock.zone(),
        ),
        doneAt = doneAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), clock.zone()) },
    )

    private fun Task.toEntity(): TaskEntity = TaskEntity(
        id = id,
        title = title,
        note = note,
        dueDate = dueDate,
        dueTime = dueTime,
        priority = when (priority) {
            TaskPriority.NONE -> com.dailycompany.data.local.task.TaskPriorityEntity.NONE
            TaskPriority.MEDIUM -> com.dailycompany.data.local.task.TaskPriorityEntity.MEDIUM
            TaskPriority.HIGH -> com.dailycompany.data.local.task.TaskPriorityEntity.HIGH
        },
        icon = icon?.serialize(),
        done = done,
        createdAt = createdAt.atZone(clock.zone()).toInstant().toEpochMilli(),
        doneAt = doneAt?.atZone(clock.zone())?.toInstant()?.toEpochMilli(),
    )

    override fun observeAll(): Flow<List<Task>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Task? = dao.getById(id)?.toDomain()

    override suspend fun save(task: Task): Long = dao.upsert(task.toEntity())

    override suspend fun setDone(id: Long, done: Boolean) {
        if (done) {
            dao.markDone(id, clock.now().atZone(clock.zone()).toInstant().toEpochMilli())
        } else {
            dao.markNotDone(id)
        }
    }

    override suspend fun setDueDate(id: Long, date: LocalDate?) = dao.setDueDate(id, date)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
