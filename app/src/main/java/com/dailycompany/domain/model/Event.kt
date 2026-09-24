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

/**
 * Serializa a regra para uma **única** coluna Room (R6: a regra é gravada, a
 * ocorrência **nunca**). Formato estável, espelhando `TaskIcon.serialize()`:
 * segmento por `|`, nomes de enum como no código.
 *
 * `fv|interval|weekDays|campo posição|fim` — ex.:
 * `"MONTHLY|2||3:FRIDAY|ON:2026-12-24"` (mensal cada 2, 3.ª sexta, até 24-dez).
 * - `weekDays`: nomes `DAY_OF_WEEK` separados por `,` (vazio = nenhum).
 * - posição: `ordinal:DayOfWeek` (ex.: `3:FRIDAY`, `-1:SUNDAY`); vazio = nula.
 * - fim: `NEVER` · `ON:yyyy-MM-dd` · `AFTER:count`.
 */
data class RecurrenceRule(
    val frequency: EventFrequency = EventFrequency.NONE,
    val interval: Int = 1,
    val weekDays: Set<DayOfWeek> = emptySet(),
    val monthPosition: MonthPosition? = null,
    val end: RecurrenceEnd = RecurrenceEnd.Never,
) {
    /** Formato estável (ver KDoc do tipo). */
    fun serialize(): String = buildString {
        append(frequency.name)
        append('|').append(interval)
        append('|').append(weekDays.joinToString(",") { it.name })
        append('|').append(monthPosition?.let { "${it.ordinal}:${it.dayOfWeek.name}" } ?: "")
        append('|').append(
            when (val e = end) {
                is RecurrenceEnd.Never -> "NEVER"
                is RecurrenceEnd.OnDate -> "ON:${e.date}"
                is RecurrenceEnd.AfterCount -> "AFTER:${e.count}"
            }
        )
    }
}

/**
 * Lê da coluna Room; valores nulos/malformados devolvem a regra predefinida
 * ([RecurrenceRule] sem repetição) — nunca bloqueia nem grava ocorrências (R6).
 */
fun parseRecurrenceRule(raw: String?): RecurrenceRule = runCatching {
    val parts = raw!!.split('|')
    require(parts.size == 5) { "formato inválido: $raw" }
    RecurrenceRule(
        frequency = EventFrequency.entries.firstOrNull { it.name == parts[0] } ?: EventFrequency.NONE,
        interval = parts[1].toInt().coerceAtLeast(1),
        weekDays = parts[2].split(",").filter { it.isNotBlank() }.mapNotNull { name ->
            DayOfWeek.entries.firstOrNull { it.name == name }
        }.toSet(),
        monthPosition = parts[3].takeIf { it.isNotBlank() }?.let { pos ->
            val (ord, dow) = pos.split(':')
            MonthPosition(ord.toInt(), DayOfWeek.entries.firstOrNull { it.name == dow } ?: return@let null)
        },
        end = when {
            parts[4] == "NEVER" -> RecurrenceEnd.Never
            parts[4].startsWith("ON:") -> RecurrenceEnd.OnDate(LocalDate.parse(parts[4].removePrefix("ON:")))
            parts[4].startsWith("AFTER:") -> RecurrenceEnd.AfterCount(parts[4].removePrefix("AFTER:").toInt().coerceAtLeast(1))
            else -> RecurrenceEnd.Never
        },
    )
}.getOrDefault(RecurrenceRule())
