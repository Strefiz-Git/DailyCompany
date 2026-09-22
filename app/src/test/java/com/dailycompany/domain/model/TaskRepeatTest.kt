package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Testes do cálculo da próxima ocorrência de uma tarefa repetitiva. */
class TaskRepeatTest {

    private val today = LocalDate.of(2026, 9, 22)

    private fun task(
        dueDate: LocalDate?,
        repeat: TaskRepeat,
    ) = Task(
        title = "t",
        dueDate = dueDate,
        repeat = repeat,
        createdAt = LocalDateTime.of(2026, 9, 1, 8, 0),
    )

    @Test
    fun `sem repeticao devolve null`() {
        assertNull(task(today, TaskRepeat.NONE).nextDueDate(today))
    }

    @Test
    fun `diaria sem data parte de hoje`() {
        assertEquals(today.plusDays(1), task(null, TaskRepeat.DAILY).nextDueDate(today))
    }

    @Test
    fun `diaria com vencimento hoje avanca para amanha`() {
        assertEquals(today.plusDays(1), task(today, TaskRepeat.DAILY).nextDueDate(today))
    }

    @Test
 fun `diaria atrasada avanca para amanha e nao para hoje`() {
        assertEquals(today.plusDays(1), task(today.minusDays(1), TaskRepeat.DAILY).nextDueDate(today))
    }

    @Test
    fun `semanal mantem o dia da semana`() {
        // base = 2026-09-15 (terça); hoje = 22/09 (terça) → próxima = 29/09 (terça)
        assertEquals(
            LocalDate.of(2026, 9, 29),
            task(LocalDate.of(2026, 9, 15), TaskRepeat.WEEKLY).nextDueDate(today),
        )
    }

    @Test
    fun `mensal com dia 31 faz clamp para o fim do mes`() {
        // base = 2026-01-31; hoje = 2026-02-28 → a próxima é 2026-03-31 (clamp)
        assertEquals(
            LocalDate.of(2026, 3, 31),
            task(LocalDate.of(2026, 1, 31), TaskRepeat.MONTHLY).nextDueDate(LocalDate.of(2026, 2, 28)),
        )
    }

    @Test
    fun `anual no dia 29 de fevereiro faz clamp para 28 de fevereiro`() {
        // base = 2028-02-29 (bissexto); hoje = 2028-03-01 → próxima = 2029-02-28 (clamp)
        assertEquals(
            LocalDate.of(2029, 2, 28),
            task(LocalDate.of(2028, 2, 29), TaskRepeat.YEARLY).nextDueDate(LocalDate.of(2028, 3, 1)),
        )
    }

    @Test
    fun `mensal atrasada salta para o mes seguinte apos hoje`() {
        // base = 2026-07-15; hoje = 2026-09-22 → próxima = 2026-10-15
        assertEquals(
            LocalDate.of(2026, 10, 15),
            task(LocalDate.of(2026, 7, 15), TaskRepeat.MONTHLY).nextDueDate(today),
        )
    }
}
