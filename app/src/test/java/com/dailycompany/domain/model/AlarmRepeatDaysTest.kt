package com.dailycompany.domain.model

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

/** Round-trip da serialização dos dias da semana de um alarme (coluna Room). */
class AlarmRepeatDaysTest {

    @Test
    fun `serializa por ordem do dia da semana e le de volta`() {
        val days = setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        val raw = serializeRepeatDays(days)
        assertEquals("MONDAY|WEDNESDAY|FRIDAY", raw)
        assertEquals(days, parseRepeatDays(raw))
        assertEquals(days, parseRepeatDays(serializeRepeatDays(days)))
    }

    @Test
    fun `set vazio serializa como string vazia`() {
        assertEquals("", serializeRepeatDays(emptySet()))
        assertEquals(emptySet<DayOfWeek>(), parseRepeatDays(""))
        assertEquals(emptySet<DayOfWeek>(), parseRepeatDays(null))
    }

    @Test
    fun `nomes invalidos sao ignorados`() {
        assertEquals(setOf(DayOfWeek.MONDAY), parseRepeatDays("MONDAY|BOGUS|NODAY"))
    }

    @Test
    fun `round trip com todos os dias`() {
        val all = DayOfWeek.entries.toSet()
        assertEquals(all, parseRepeatDays(serializeRepeatDays(all)))
    }
}