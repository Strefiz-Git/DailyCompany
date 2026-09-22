package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskGroupsTest {

    private val today = LocalDate.of(2026, 9, 22)

    private fun task(
        id: Long,
        dueDate: LocalDate? = null,
        done: Boolean = false,
        priority: TaskPriority = TaskPriority.NONE,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 9, 1, 8, 0),
    ) = Task(
        id = id,
        title = "Tarefa $id",
        dueDate = dueDate,
        priority = priority,
        done = done,
        createdAt = createdAt,
    )

    @Test
    fun `groupTasks separa pelas abas`() {
        val tasks = listOf(
            task(1, dueDate = today),
            task(2, dueDate = today.minusDays(1)),
            task(3, dueDate = today.plusDays(2)),
            task(4, dueDate = null),
            task(5, done = true),
        )

        val groups = groupTasks(tasks, today)

        assertEquals(listOf(1L), groups.today.map { it.id })
        assertEquals(listOf(2L), groups.overdue.map { it.id })
        assertEquals(listOf(3L), groups.upcoming.map { it.id })
        assertEquals(listOf(4L), groups.noDate.map { it.id })
        assertEquals(listOf(5L), groups.done.map { it.id })
        assertFalse(groups.isEmpty)
    }

    @Test
    fun `groupTasks ordena por prioridade e data`() {
        val tasks = listOf(
            task(1, dueDate = today.plusDays(3), priority = TaskPriority.HIGH),
            task(2, dueDate = today.plusDays(1), priority = TaskPriority.MEDIUM),
            task(3, dueDate = today.plusDays(2), priority = TaskPriority.HIGH),
            task(4, dueDate = today.plusDays(1)),
        )

        val groups = groupTasks(tasks, today)

        // HIGH primeiro (por data), depois MEDIUM, depois NONE.
        assertEquals(listOf(3L, 1L, 2L, 4L), groups.upcoming.map { it.id })
    }

    @Test
    fun `groupTasks vazio`() {
        val groups = groupTasks(emptyList(), today)
        assertTrue(groups.isEmpty)
        assertTrue(groups.today.isEmpty())
        assertTrue(groups.overdue.isEmpty())
        assertTrue(groups.upcoming.isEmpty())
        assertTrue(groups.noDate.isEmpty())
        assertTrue(groups.done.isEmpty())
    }

    @Test
    fun `groupTasks concluidas nao contam como atrasadas`() {
        val tasks = listOf(task(1, dueDate = today.minusDays(5), done = true))
        val groups = groupTasks(tasks, today)
        assertEquals(listOf(1L), groups.done.map { it.id })
        assertTrue(groups.overdue.isEmpty())
        assertTrue(groups.today.isEmpty())
    }
}