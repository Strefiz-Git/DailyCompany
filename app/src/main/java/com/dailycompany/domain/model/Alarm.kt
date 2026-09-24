package com.dailycompany.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Alarme — modelo de domínio puro (java.time, sem Android/Room).
 *
 * - [time] é hora de "parede" (wall-clock): toca àquela hora no fuso atual e é
 *   reagendado em `TIMEZONE_CHANGED` (o agendamento converte para milissegundos
 *   no momento de agendar).
 * - [repeatDays] vazio = toca uma vez (um disparo, depois desliga-se);
 *   com dias escolhidos = repete nesses dias da semana.
 */
data class Alarm(
    val id: Long = 0L,
    val uid: String,
    val time: LocalTime,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val autoSnoozeMinutes: Int = 0,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime,
)

/**
 * Próximo momento em que o alarme deve tocar, **estritamente depois de [now]**:
 * - sem dias escolhidos ([repeatDays] vazio): hoje à hora se ainda não passou,
 *   senão amanhã à mesma hora;
 * - com dias escolhidos: o próximo dia (≥ hoje) cujo dia da semana está em
 *   [repeatDays] — "hoje" só conta se a hora ainda não tiver passado.
 *
 * Casos de borda tratados: hora igual a [now] conta como "já passou" (avança);
 * uma regra com todos os 7 dias toca sempre no dia seguinte.
 */
fun Alarm.nextTrigger(now: LocalDateTime): LocalDateTime {
    val today = now.toLocalDate()
    if (repeatDays.isEmpty()) {
        val todayAtTime = today.atTime(time)
        return if (todayAtTime.isAfter(now)) todayAtTime else today.plusDays(1).atTime(time)
    }
    for (daysAhead in 0L..7L) {
        val candidate = today.plusDays(daysAhead).atTime(time)
        if (candidate.dayOfWeek in repeatDays && (daysAhead > 0 || candidate.isAfter(now))) {
            return candidate
        }
    }
    // 8 dias consecutivos cobrem todos os dias da semana — inalcançável quando
    // repeatDays não está vazio (mantém a função total, sem `!!`).
    return today.plusDays(7).atTime(time)
}

/** Dias da semana para a coluna Room: nomes separados por `|` (vazio = sem repetição). */
fun serializeRepeatDays(days: Set<DayOfWeek>): String =
    days.sortedBy { it.value }.joinToString(separator = "|") { it.name }

/** Lê a coluna Room; valores malformados são ignorados (devolve set vazio). */
fun parseRepeatDays(raw: String?): Set<DayOfWeek> =
    raw.orEmpty().split("|").mapNotNull { name ->
        DayOfWeek.entries.firstOrNull { it.name == name }
    }.toSet()