package com.dailycompany.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Frequência de repetição de um evento. A ocorrência **nunca é gravada** na
 * BD (R6) — é sempre calculada a partir da regra ([RecurrenceRule]) por
 * [OccurrenceCalculator].
 */
enum class EventFrequency {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

/**
 * Posição mensal de uma ocorrência: "mesma data" (ex.: dia 15) ou "mesma
 * posição" (ex.: 3.ª sexta, último domingo).
 *
 * - [ordinal] 1..5 (ou -1 para "último") e [dayOfWeek] — usado quando
 *   [RecurrenceRule.monthPosition] está preenchido.
 */
data class MonthPosition(
    val ordinal: Int,
    val dayOfWeek: DayOfWeek,
)

/**
 * Fim de uma regra de repetição. Sempre calculado no momento da expansão,
 * nunca persistido como ocorrências (R6).
 */
sealed interface RecurrenceEnd {
    data object Never : RecurrenceEnd
    data class OnDate(val date: LocalDate) : RecurrenceEnd
    data class AfterCount(val count: Int) : RecurrenceEnd
}

/**
 * Regra de repetição de um evento — domínio puro, serializado como String
 * para uma única coluna Room (formato estável em [serialize]/[parseRecurrenceRule],
 * espelhando a convenção `TaskIcon.serialize()`).
 *
 * Modelo de decisões (documentado):
 * - [interval] ≥ 1 é "cada N" unidades da [frequency] (ex.: mensal cada 2 meses).
 * - [weekDays] só faz sentido em WEEKLY ("dias da semana": seg, qua, sex).
 * - [monthPosition] só em MONTHLY quando se repete "na mesma posição".
 * - Fim: nunca / numa data / após N ocorrências (nunca armazenado).
 */
data class RecurrenceRule(
    val frequency: EventFrequency = EventFrequency.NONE,
    val interval: Int = 1,
    val weekDays: Set<DayOfWeek> = emptySet(),
    val monthPosition: MonthPosition? = null,
    val end: RecurrenceEnd = RecurrenceEnd.Never,
) {

    init {
        require(interval >= 1) { "interval deve ser >= 1 (era $interval)" }
    }
}

/**
 * Evento — modelo de domínio puro (java.time, sem Android/Room).
 * A ocorrência é calculada por [OccurrenceCalculator]; nunca gravada (R6).
 */
data class Event(
    val id: Long = 0L,
    val uid: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val notes: String? = null,
    val colorKey: String? = null,
    val recurrence: RecurrenceRule = RecurrenceRule(),
    val createdAt: LocalDateTime,
)
