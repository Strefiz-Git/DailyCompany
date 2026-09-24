package com.dailycompany.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes do calculador de ocorrências — Fase 2 (Eventos), Passo 1: domínio
 * puro + testes. Cobre o critério "pronto quando" — o aniversário anual gera
 * **1 ocorrência/ano** entre duas datas durante **3 meses** — mais os clamps
 * e o fim de série.
 *
 * Datas explícitas (sem Clock), determinístico.
 */
class EventOccurrenceCalculatorTest {

    // Âncora: 2026-09-22 (3.ª feira).
    private val base = LocalDate.of(2026, 9, 22)

    private val yearly = RecurrenceRule(frequency = EventFrequency.YEARLY)
    private val monthly = RecurrenceRule(frequency = EventFrequency.MONTHLY)
    private val weekly = RecurrenceRule(frequency = EventFrequency.WEEKLY)
    private val daily = RecurrenceRule(frequency = EventFrequency.DAILY)

    private fun event(
        date: LocalDate,
        rule: RecurrenceRule = RecurrenceRule(),
    ) = Event(
        uid = "event:test",
        title = "Aniversário",
        date = date,
        recurrence = rule,
        createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    )

    @Test
    fun `aniversario anual gera uma ocorrencia por ano nos 3 meses`() {
        // from = 22-set-26 .. to = 22-dez-26 ⇒ 1 ocorrência (o próprio dia)
        val from = base
        val to = base.plusMonths(3)
        assertEquals(
            listOf(LocalDate.of(2026, 9, 22)),
            OccurrenceCalculator.occurrences(event(base, yearly), from, to),
        )
    }

    @Test
    fun `anual cruza a virada do ano gera 2 ocorrencias no intervalo`() {
        // from = base (2026-09-22) .. to = +12 meses ⇒ 22-set-26 e 22-set-27
        val from = base
        val to = base.plusMonths(12)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2027, 9, 22),
            ),
            OccurrenceCalculator.occurrences(event(base, yearly), from, to),
        )
    }

    @Test
    fun `aniversario 29 de fevereiro faz clamp para 28 em ano nao bissexto`() {
        // aniversário 2000-02-29; 2026 não é bissexto ⇒ 28-fev-26
        val birth = LocalDate.of(2000, 2, 29)
        val from = LocalDate.of(2026, 1, 1)
        val to = LocalDate.of(2026, 12, 31)
        assertEquals(
            listOf(LocalDate.of(2026, 2, 28)),
            OccurrenceCalculator.occurrences(event(birth, yearly), from, to),
        )
    }

    @Test
    fun `mensal dia 31 faz clamp para o fim do mes e volta ao 31 no seguinte`() {
        // Jan 31 → Fev 28 (clamp) → **Mar 31** (volta ao dia original)
        val from = LocalDate.of(2026, 1, 1)
        val to = LocalDate.of(2026, 4, 1)
        assertEquals(
            listOf(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31),
            ),
            OccurrenceCalculator.occurrences(event(LocalDate.of(2026, 1, 31), monthly), from, to),
        )
    }

    @Test
    fun `mensal mesma posicao 3a sexta do mes`() {
        val rule = RecurrenceRule(
            frequency = EventFrequency.MONTHLY,
            monthPosition = MonthPosition(3, java.time.DayOfWeek.FRIDAY),
        )
        val from = LocalDate.of(2026, 9, 1)
        val to = LocalDate.of(2026, 12, 31)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 10, 16),
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 12, 18),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `mensal mesma posicao ultimo domingo do mes`() {
        val rule = RecurrenceRule(
            frequency = EventFrequency.MONTHLY,
            monthPosition = MonthPosition(-1, java.time.DayOfWeek.SUNDAY),
        )
        val from = LocalDate.of(2026, 9, 1)
        val to = LocalDate.of(2026, 11, 30)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 27),
                LocalDate.of(2026, 10, 25),
                LocalDate.of(2026, 11, 29),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `semanal com dias escolhidos seg qua sex na semana da virada`() {
        val rule = RecurrenceRule(
            frequency = EventFrequency.WEEKLY,
            weekDays = setOf(
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.FRIDAY,
            ),
        )
        val from = LocalDate.of(2026, 9, 22)
        val to = LocalDate.of(2026, 9, 30)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 23),
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 9, 30),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `semanal sem dias escolhidos repete o mesmo dia da semana`() {
        val rule = RecurrenceRule(frequency = EventFrequency.WEEKLY, interval = 2)
        val from = LocalDate.of(2026, 9, 22)
        val to = LocalDate.of(2026, 10, 31)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 20),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `diaria a cada 2 dias gera dias alternados`() {
        val rule = RecurrenceRule(frequency = EventFrequency.DAILY, interval = 2)
        val from = LocalDate.of(2026, 9, 22)
        val to = LocalDate.of(2026, 9, 30)
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 24),
                LocalDate.of(2026, 9, 26),
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 9, 30),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `sem repeticao devolve so a propria data`() {
        val from = base
        val to = base.plusMonths(1)
        assertEquals(
            listOf(LocalDate.of(2026, 9, 22)),
            OccurrenceCalculator.occurrences(event(base, RecurrenceRule()), from, to),
        )
    }

    @Test
    fun `sem repeticao fora do intervalo devolve vazio`() {
        val from = LocalDate.of(2026, 10, 1)
        val to = LocalDate.of(2026, 12, 31)
        assertEquals(
            emptyList<LocalDate>(),
            OccurrenceCalculator.occurrences(event(base, RecurrenceRule()), from, to),
        )
    }

    @Test
    fun `fim numa data corta as ocorrencias depois dessa data`() {
        val rule = RecurrenceRule(
            frequency = EventFrequency.YEARLY,
            end = RecurrenceEnd.OnDate(LocalDate.of(2027, 6, 30)),
        )
        val from = base
        val to = base.plusYears(1).plusMonths(3) // 2027-12-22
        assertEquals(
            listOf(LocalDate.of(2026, 9, 22)),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `apos N ocorrencias corta a serie`() {
        val rule = RecurrenceRule(
            frequency = EventFrequency.YEARLY,
            end = RecurrenceEnd.AfterCount(2),
        )
        val from = base
        val to = base.plusYears(1).plusMonths(3) // 2027-12-22
        assertEquals(
            listOf(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2027, 9, 22),
            ),
            OccurrenceCalculator.occurrences(event(base, rule), from, to),
        )
    }

    @Test
    fun `nextOccurrence devolve a proxima ocorrencia depois de uma data`() {
        val after = LocalDate.of(2026, 12, 1)
        assertEquals(
            LocalDate.of(2027, 9, 22),
            OccurrenceCalculator.nextOccurrence(event(base, yearly), after),
        )
    }

    @Test
    fun `sem repeticao nao tem proxima ocorrencia`() {
        assertNull(
            OccurrenceCalculator.nextOccurrence(event(base, RecurrenceRule()), base),
        )
    }
}
