package com.dailycompany.feature.events

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da grelha do calendário mensal (Passo 4 — Agenda, SCREENS.md §4). */
class MonthGridTest {

    private fun leadingNulls(cells: List<LocalDate?>) = cells.takeWhile { it == null }.size

    private fun trailingNulls(cells: List<LocalDate?>) = cells.takeLastWhile { it == null }.size

    @Test
    fun `janeiro 2026 comeca quinta e abre com 3 celulas vazias`() {
        val month = YearMonth.of(2026, 1)
        assertEquals(DayOfWeek.THURSDAY, month.atDay(1).dayOfWeek)
        val cells = monthCells(month)
        assertEquals(3, leadingNulls(cells))
        assertEquals(31, cells.count { it != null })
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun `junho 2026 comeca segunda e nao tem celulas vazias no inicio`() {
        val month = YearMonth.of(2026, 6)
        assertEquals(DayOfWeek.MONDAY, month.atDay(1).dayOfWeek)
        val cells = monthCells(month)
        assertEquals(0, leadingNulls(cells))
        assertEquals(30, cells.count { it != null })
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun `fevereiro bissexto 2024 tem 29 dias`() {
        val month = YearMonth.of(2024, 2)
        val cells = monthCells(month)
        assertEquals(29, cells.count { it != null })
        assertEquals(3, leadingNulls(cells))
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun `fevereiro 2026 nao bissexto tem 28 dias e completa a ultima semana`() {
        val month = YearMonth.of(2026, 2)
        val cells = monthCells(month)
        assertEquals(28, cells.count { it != null })
        assertEquals(6, leadingNulls(cells))
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun `cada dia aparece exatamente uma vez na posicao certa`() {
        val month = YearMonth.of(2026, 3)
        val cells = monthCells(month)
        val days = cells.filterNotNull()
        assertEquals(31, days.size)
        assertEquals(LocalDate.of(2026, 3, 1), days.first())
        assertEquals(LocalDate.of(2026, 3, 31), days.last())
        assertEquals(days.size, days.toSet().size)
    }

    @Test
    fun `mes com 30 dias que comeca a segunda gera 5 semanas`() {
        assertEquals(5, monthCells(YearMonth.of(2026, 6)).size / 7)
    }

    @Test
    fun `mes que comeca a domingo gera 6 semanas`() {
        assertEquals(6, monthCells(YearMonth.of(2026, 3)).size / 7)
    }

    @Test
    fun `celulas nulas aparecem so nas margens do mes`() {
        val cells = monthCells(YearMonth.of(2026, 4))
        // Abril de 2026 começa a quarta: 2 nulas à frente, 30 dias, mais as de fecho.
        assertEquals(2, leadingNulls(cells))
        assertNull(cells.first())
        assertTrue(cells.filterNotNull().all { YearMonth.from(it) == YearMonth.of(2026, 4) })
        assertEquals(30, cells.count { it != null })
        assertEquals(0, cells.size % 7)
        // Nunca há dois meses no mesmo mês nem nulas pelo meio.
        val seenDay = mutableSetOf<Int>()
        cells.filterNotNull().forEach { assertTrue(seenDay.add(it.dayOfMonth)) }
    }
}