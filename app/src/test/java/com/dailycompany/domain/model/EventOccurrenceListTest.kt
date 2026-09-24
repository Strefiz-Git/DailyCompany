package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes do corte "Agenda" (Fase 2, Passo 3): `upcomingOccurrences` expande
 * as regras na janela [hoje .. +3 meses] (critério "pronto quando" — consulta
 * a 3 meses) e ordena. Datas explícitas, sem Clock.
 */
class EventOccurrenceListTest {

    private val today = LocalDate.of(2026, 9, 22)

    private fun event(
        title: String = "Evento $idCounter",
        date: LocalDate,
        time: LocalTime? = null,
        isAllDay: Boolean = false,
        recurrence: RecurrenceRule = RecurrenceRule(),
    ) = Event(
        uid = "event:${idCounter++}",
        title = title,
        date = date,
        time = time,
        isAllDay = isAllDay,
        recurrence = recurrence,
        createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    )

    @Test
    fun `evento pontual dentro da janela aparece`() {
        val e = event(date = today.plusMonths(1))
        assertEquals(
            listOf(EventOccurrence(e, today.plusMonths(1))),
            upcomingOccurrences(listOf(e), today, today.plusMonths(3)),
        )
    }

    @Test
    fun `evento pontual fora da janela nao aparece`() {
        val past = event(date = today.minusMonths(1))
        val far = event(date = today.plusMonths(6))
        assertEquals(
            emptyList<EventOccurrence>(),
            upcomingOccurrences(listOf(past, far), today, today.plusMonths(3)),
        )
    }

    @Test
    fun `eventos vazios devolvem lista vazia`() {
        assertEquals(
            emptyList<EventOccurrence>(),
            upcomingOccurrences(emptyList(), today, today.plusMonths(3)),
        )
    }

    @Test
    fun `repeticao diaria expande varias datas na janela`() {
        val e = event(
            date = today,
            recurrence = RecurrenceRule(frequency = EventFrequency.DAILY),
        )
        val occurrences = upcomingOccurrences(listOf(e), today, today.plusDays(2))
        assertEquals(
            listOf(today, today.plusDays(1), today.plusDays(2)),
            occurrences.map { it.date },
        )
    }

    @Test
    fun `aniversario anual aparece na janela de 3 meses`() {
        val e = event(
            date = today,
            recurrence = RecurrenceRule(frequency = EventFrequency.YEARLY),
        )
        val occurrences = upcomingOccurrences(listOf(e), today, today.plusMonths(3))
        assertEquals(listOf(today), occurrences.map { it.date })
    }

    @Test
    fun `ocorrencias ordenadas por data e hora`() {
        val a = event(title = "Segunda", date = today.plusDays(1), time = LocalTime.of(9, 0))
        val b = event(title = "Primeira", date = today.plusDays(1), time = LocalTime.of(8, 0))
        val c = event(title = "Dia inteiro", date = today.plusDays(1), isAllDay = true)
        val occurrences = upcomingOccurrences(listOf(a, b, c), today, today.plusMonths(3))
        assertEquals(
            listOf("Dia inteiro", "Primeira", "Segunda"),
            occurrences.map { it.event.title },
        )
    }
}

// Contador local para uids/títulos únicos nos testes (não usa Clock).
private var idCounter = 0