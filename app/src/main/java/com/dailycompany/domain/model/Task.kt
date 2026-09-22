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
 * Repetição simples de uma tarefa. A ocorrência seguinte é calculada a partir
 * de [Task.dueDate]; sem data, parte do dia atual.
 */
enum class TaskRepeat {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
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
    val icon: TaskIcon? = null,
    val repeat: TaskRepeat = TaskRepeat.NONE,
    val done: Boolean = false,
    val createdAt: LocalDateTime,
    val doneAt: LocalDateTime? = null,
)

/**
 * Próxima ocorrência de uma tarefa repetitiva, estritamente depois de [today],
 * alinhada à periodicidade da repetição a partir da data de vencimento.
 * - Sem repetição → `null`.
 * - Dias 29–31 de meses curtos e 29 de fevereiro "dobram" para o fim do mês
 *   (clamping nativo de `plusMonths`/`plusYears`) — decisão documentada.
 * - Tarefa repetitiva sem [Task.dueDate] parte do dia de hoje.
 * - Atrasos não saltam o alinhamento: a ocorrência seguinte mantém o dia da
 *   semana/mês da data de vencimento original.
 */
fun Task.nextDueDate(today: LocalDate): LocalDate? {
    if (repeat == TaskRepeat.NONE) return null
    val base = dueDate ?: today
    // Avanço sempre a partir da [base] com um contador: depois de um clamp
    // (ex.: dia 31 → fim de mês) o passo seguinte tem de voltar ao dia da base
    // (Jan 31 → Fev 28 → **Mar 31**, não Mar 28).
    var count = 1L
    while (true) {
        val candidate = when (repeat) {
            TaskRepeat.DAILY -> base.plusDays(count)
            TaskRepeat.WEEKLY -> base.plusWeeks(count)
            TaskRepeat.MONTHLY -> base.plusMonths(count)
            TaskRepeat.YEARLY -> base.plusYears(count)
            TaskRepeat.NONE -> return null
        }
        if (candidate > today) return candidate
        count++
    }
}

/** Tarefa criada a partir dos campos de "nova tarefa". */
fun newTask(
    title: String,
    note: String,
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    priority: TaskPriority,
    icon: TaskIcon? = null,
    repeat: TaskRepeat = TaskRepeat.NONE,
    createdAt: LocalDateTime,
): Task = Task(
    title = title,
    note = note,
    dueDate = dueDate,
    dueTime = dueTime,
    priority = priority,
    icon = icon,
    repeat = repeat,
    createdAt = createdAt,
)