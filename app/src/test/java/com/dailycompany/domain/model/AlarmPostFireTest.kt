package com.dailycompany.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Testes da decisão pura pós-disparo (repete → reagendar; de uma vez → desligar). */
class AlarmPostFireTest {

    // 2026-09-24 é quinta-feira.
    private val now = LocalDateTime.of(2026, 9, 24, 7, 0)

    private fun alarm(days: Set<DayOfWeek>, autoSnoozeMinutes: Int = 0) = Alarm(
        uid = "u1",
        time = LocalTime.of(7, 0),
        repeatDays = days,
        autoSnoozeMinutes = autoSnoozeMinutes,
        createdAt = now,
    )

    @Test
    fun `one shot desliga apos o disparo sem snooze automatico`() {
        val action = alarm(emptySet()).postFireAction(now) as AlarmPostFire.Disable
        assertNull(action.autoSnoozeAt)
    }

    @Test
    fun `one shot com auto snooze agenda o toque adiado`() {
        val action = alarm(emptySet(), autoSnoozeMinutes = 10).postFireAction(now)
            as AlarmPostFire.Disable
        assertEquals(now.plusMinutes(10), action.autoSnoozeAt)
    }

    @Test
    fun `alarme repetido reagenda para o proximo disparo`() {
        val action = alarm(setOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)).postFireAction(now)
            as AlarmPostFire.Reschedule
        assertEquals(LocalDateTime.of(2026, 9, 25, 7, 0), action.next)
        assertNull(action.autoSnoozeAt)
    }

    @Test
    fun `alarme repetido com auto snooze agenda ambos`() {
        val action = alarm(setOf(DayOfWeek.THURSDAY), autoSnoozeMinutes = 5).postFireAction(now)
            as AlarmPostFire.Reschedule
        assertEquals(LocalDateTime.of(2026, 10, 1, 7, 0), action.next) // próxima quinta
        assertEquals(now.plusMinutes(5), action.autoSnoozeAt)
    }
}