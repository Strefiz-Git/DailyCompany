package com.dailycompany.domain.model

import java.time.LocalDate

/**
 * Agrupamento das tarefas pelos separadores do ecrã Tarefas.
 * Função pura: dado o conjunto de tarefas e a data "hoje", devolve a sua
 * divisão. Sem Clock, sem Android — totalmente testável.
 */
data class TaskGroups(
    val today: List<Task> = emptyList(),
    val overdue: List<Task> = emptyList(),
    val upcoming: List<Task> = emptyList(),
    val noDate: List<Task> = emptyList(),
    val done: List<Task> = emptyList(),
) {
    val isEmpty: Boolean
        get() = today.isEmpty() && overdue.isEmpty() && upcoming.isEmpty() && noDate.isEmpty() && done.isEmpty()
}

private fun sortedTasks(tasks: List<Task>): List<Task> = tasks.sortedWith(
    compareByDescending<Task> { it.priority.ordinal }
        .thenBy { it.dueDate }
        .thenByDescending { it.createdAt },
)

/**
 * Divide as tarefas ativas nos grupos "Hoje"/"Atrasadas"/"Próximas"/"Sem data"
 * e as concluídas à parte.
 */
fun groupTasks(tasks: List<Task>, today: LocalDate): TaskGroups {
    val noDate = mutableListOf<Task>()
    val todayList = mutableListOf<Task>()
    val overdue = mutableListOf<Task>()
    val upcoming = mutableListOf<Task>()
    val done = mutableListOf<Task>()

    tasks.forEach { task ->
        when {
            task.done -> done.add(task)
            task.dueDate == null -> noDate.add(task)
            task.dueDate < today -> overdue.add(task)
            task.dueDate == today -> todayList.add(task)
            else -> upcoming.add(task)
        }
    }

    return TaskGroups(
        today = sortedTasks(todayList),
        overdue = sortedTasks(overdue),
        upcoming = sortedTasks(upcoming),
        noDate = sortedTasks(noDate),
        done = sortedTasks(done),
    )
}
