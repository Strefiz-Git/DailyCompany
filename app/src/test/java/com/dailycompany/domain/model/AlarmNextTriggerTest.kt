package com.dailycompany.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes do próximo disparo do alarme (Fase 3 — horas iguais a "agora" contam como passadas). */
class AlarmNextTriggerTest {

    // 2026-09-24 é quinta-feira (confirmado na fase anterior).
    private val now = LocalDateTime.of(2026, 9, 24, 10, 0)

    private fun alarm(time: LocalTime, days: Set<DayOfWeek> = emptySet()) = Alarm(
        uid = "u1",
        time = time,
        repeatDays = days,
        createdAt = now,
    )

    @Test
    fun `sem dias escolhidos e hora ainda nao passou toca hoje`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 24, 18, 30),
            alarm(LocalTime.of(18, 30)).nextTrigger(now),
        )
    }

    @Test
    fun `sem dias escolhidos e hora ja passou toca amanha`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 25, 7, 0),
            alarm(LocalTime.of(7, 0)).nextTrigger(now),
        )
    }

    @Test
    fun `hora exatamente igual a agora avanca para o proximo dia`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 25, 10, 0),
            alarm(LocalTime.of(10, 0)).nextTrigger(now),
        )
    }

    @Test
    fun `com dias e o dia de hoje ja passou avanca para o proximo dia escolhido`() {
        val days = setOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)
        assertEquals(
            LocalDateTime.of(2026, 9, 26, 7, 0), // sábado
            alarm(LocalTime.of(7, 0), days).nextTrigger(now),
        )
    }

    @Test
    fun `com dias e o dia de hoje ainda nao passou toca hoje`() {
        val days = setOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)
        assertEquals(
            LocalDateTime.of(2026, 9, 24, 18, 30),
            alarm(LocalTime.of(18, 30), days).nextTrigger(now),
        )
    }

    @Test
    fun `um unico dia da semana salta para o proximo`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 28, 7, 0), // segunda-feira
            alarm(LocalTime.of(7, 0), setOf(DayOfWeek.MONDAY)).nextTrigger(now),
        )
    }

    @Test
    fun `todos os dias da semana toca no dia seguinte`() {
        assertEquals(
            LocalDateTime.of(2026, 9, 25, 7, 0), // sexta-feira
            alarm(LocalTime.of(7, 0), DayOfWeek.entries.toSet()).nextTrigger(now),
        )
    }

    @Test
    fun `um alarme a meia-noite depois da meia-noite toca no dia seguinte`() {
        val midnight = LocalDateTime.of(2026, 9, 25, 0, 30)
        assertEquals(
            LocalDateTime.of(2026, 9, 26, 0, 0),
            alarm(LocalTime.MIDNIGHT).nextTrigger(midnight),
        )
    }
}