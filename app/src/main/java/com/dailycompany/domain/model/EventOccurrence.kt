package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Uma data concreta de um evento na Agenda. **R6 (inviolável)**: a ocorrência
 * nunca é gravada na BD — é calculada a partir da regra por
 * [OccurrenceCalculator] e vive apenas no estado da UI.
 */
data class EventOccurrence(
    val event: Event,
    val date: LocalDate,
)

/**
 * Expande todos os [events] na janela [from..to] (inclusive) e devolve as
 * ocorrências ordenadas por data (dia inteiro primeiro, depois pela hora) e,
 * em caso de empate, por título. Sempre calculado a partir da regra — R6.
 *
 * Usado pela Agenda ("próximos eventos agrupados por mês") e pelos testes.
 */
fun upcomingOccurrences(
    events: List<Event>,
    from: LocalDate,
    to: LocalDate,
): List<EventOccurrence> =
    events
        .flatMap { event ->
            OccurrenceCalculator.occurrences(event, from, to).map { date -> EventOccurrence(event, date) }
        }
        .sortedWith(
            compareBy<EventOccurrence> { it.date }
                .thenBy { it.event.time ?: LocalTime.MIN }
                .thenBy { it.event.title.lowercase() }
                .thenBy { it.event.uid },
        )