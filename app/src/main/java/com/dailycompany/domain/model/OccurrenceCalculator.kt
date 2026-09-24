package com.dailycompany.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Calculador de ocorrências de um evento — **R6 / regra inviolável**: uma
 * ocorrência **nunca é gravada** na BD; é sempre calculada a partir da
 * [RecurrenceRule] para o intervalo pedido.
 *
 * Expandir uma regra num intervalo [from..to] (inclusive), com o aniversário
 * anual (Fase 2) como caso de uso central: 1 ocorrência/ano entre duas datas
 * durante 3 meses funciona.
 *
 * Decisões (espelham `Task.nextDueDate`, documentadas):
 * - [EventFrequency.NONE] ⇒ exatamente uma ocorrência (a data do evento), se
 *   cair no intervalo.
 * - [EventFrequency.DAILY]: data do evento, depois a cada [RecurrenceRule.interval]
 *   dias.
 * - [EventFrequency.WEEKLY] com dias escolhidos: esses dias da semana, cada
 *   `interval` semanas, ancorados na semana (2.ª-feira) da data do evento;
 *   nunca antes da data do evento.
 * - [EventFrequency.WEEKLY] sem dias: mesmo dia da semana, cada `interval`
 *   semanas.
 * - [EventFrequency.MONTHLY] "mesmo dia": dia 29–31 e 29-fev fazem *clamp*
 *   para o fim do mês; o mês seguinte **volta ao dia original** (Jan 31 →
 *   Fev 28 → **Mar 31**, não Mar 28) — idêntico ao modelo das tarefas.
 * - [EventFrequency.MONTHLY] "mesma posição": 3.ª sexta (ordinal positivo) ou
 *   último domingo (ordinal negativo) desse mês; se a posição não existir
 *   nesse mês, omite-se (não clamp).
 * - [EventFrequency.YEARLY] (aniversário): mesma data cada N anos, com clamp
 *   29-fev→28-fev em anos não bissextos.
 * - Fim: [RecurrenceEnd.Never] · [RecurrenceEnd.OnDate] (limite superior) ·
 *   [RecurrenceEnd.AfterCount] (limite global de N ocorrências da série,
 *   contadas a partir da âncora do evento).
 */
object OccurrenceCalculator {

    /**
     * Todas as ocorrências de [event] entre [from] (incl.) e [to] (incl.),
     * cronológicas. Usado pela agenda/calendário e pelos testes da Fase 2
     * ("consulta a 3 meses"). O fim da série (OnDate/AfterCount) é aplicado
     * aqui — as ocorrências **nunca** ficam na BD.
     */
    fun occurrences(event: Event, from: LocalDate, to: LocalDate): List<LocalDate> {
        require(!to.isBefore(from))
        val rule = event.recurrence
        val base = event.date
        val interval = rule.interval.coerceAtLeast(1)

        if (rule.frequency == EventFrequency.NONE) {
            return if (base in from..to) listOf(base) else emptyList()
        }

        val maxEnd = minOf(to, rule.end.inclusiveLimit())
        val countLimit = (rule.end as? RecurrenceEnd.AfterCount)?.count

        val result = ArrayList<LocalDate>()
        var seen = 0L

        /** Emite [date] (se no intervalo) e conta a ocorrência da série;
         *  devolve `false` quando a série já produziu as [countLimit] ocorrências. */
        fun emit(date: LocalDate): Boolean {
            if (countLimit != null && seen >= countLimit.toLong()) return false
            seen++
            if (date in from..to) result += date
            return true
        }

        when (rule.frequency) {
            EventFrequency.DAILY -> {
                var date = base
                if (date < from) {
                    val days = ChronoUnit.DAYS.between(base, from)
                    date = base.plusDays(((days / interval + 1) * interval).toLong())
                }
                while (date <= maxEnd) {
                    if (!emit(date)) break
                    date = date.plusDays(interval.toLong())
                }
            }

            EventFrequency.WEEKLY -> {
                val days = rule.weekDays
                if (days.isEmpty()) {
                    // Mesmo dia da semana, cada `interval` semanas.
                    var date = base
                    if (date < from) {
                        val weeks = ChronoUnit.WEEKS.between(base, from)
                        date = base.plusWeeks(((weeks / interval + 1) * interval).toLong())
                    }
                    while (date <= maxEnd) {
                        if (!emit(date)) break
                        date = date.plusWeeks(interval.toLong())
                    }
                } else {
                    // Dias escolhidos (seg/qua/sex), cada `interval` semanas,
                    // ancorados na semana (2.ª-feira) da data do evento.
                    val weekStart = base.with(DayOfWeek.MONDAY)
                    var week = weekStart
                    // Avança por blocos de `interval` semanas até um bloco
                    // com alguma data possível dentro de `from..maxEnd`.
                    while (true) {
                        val blockEnd = week.plusDays(6)
                        if (blockEnd >= from) break
                        if (week.plusWeeks(interval.toLong()) > maxEnd) break
                        week = week.plusWeeks(interval.toLong())
                    }
                    while (week <= maxEnd) {
                        for (d in days.sortedBy { it.value }) {
                            val candidate = alignToDay(week, d)
                            if (candidate >= base && candidate in from..to && candidate <= maxEnd) {
                                if (!emit(candidate)) return result
                            }
                        }
                        week = week.plusWeeks(interval.toLong())
                    }
                }
            }

            EventFrequency.MONTHLY -> {
                val position = rule.monthPosition
                if (position == null) {
                    // "Mesmo dia" com clamp; a âncora `base.day` garante voltar
                    // ao dia original no mês seguinte (Jan 31 → Fev 28 → Mar 31).
                    var month = YearMonth.from(base)
                    while (true) {
                        val candidate = clampMonthDay(month.atDay(base.dayOfMonth.coerceAtMost(month.lengthOfMonth())))
                        if (candidate >= maxEnd) break
                        if (candidate >= from) break
                        month = month.plusMonths(interval.toLong())
                        if (clampMonthDay(month.atDay(base.dayOfMonth.coerceAtMost(month.lengthOfMonth()))) > maxEnd) break
                    }
                    while (clampMonthDay(month.atDay(base.dayOfMonth.coerceAtMost(month.lengthOfMonth()))) <= maxEnd) {
                        val candidate = clampMonthDay(month.atDay(base.dayOfMonth.coerceAtMost(month.lengthOfMonth())))
                        if (candidate in from..to) {
                            if (!emit(candidate)) break
                        } else if (candidate > maxEnd) break
                        month = month.plusMonths(interval.toLong())
                    }
                } else {
                    // "Mesma posição": 3.ª sexta / último domingo de cada
                    // `interval` meses. Se a posição não existir nesse mês,
                    // omitida (não clamp).
                    var month = YearMonth.from(base)
                    while (month.atDay(1) <= maxEnd) {
                        val candidate = nthOrLast(month, position)
                        if (candidate != null && candidate in from..to && candidate <= maxEnd) {
                            if (!emit(candidate)) break
                        }
                        month = month.plusMonths(interval.toLong())
                    }
                }
            }

            EventFrequency.YEARLY -> {
                // Aniversário: mesma data cada `interval` anos, com clamp
                // 29-fev→28-fev (e 29-fev→28 em ano não bissexto).
                var date = base
                if (date < from) {
                    val years = ChronoUnit.YEARS.between(base, from)
                    date = base.plusYears(((years / interval + 1) * interval).toLong())
                }
                while (date <= maxEnd) {
                    if (!emit(clampYearDay(date))) break
                    date = date.plusYears(interval.toLong())
                }
            }
        }
        return result
    }

    /**
     * Próxima ocorrência estritamente depois de [after]. Para eventos anuais
     * (aniversários) devolve a ocorrência do ano seguinte, se existir; senão
     * null. `NONE` ⇒ null.
     */
    fun nextOccurrence(event: Event, after: LocalDate, today: LocalDate = after): LocalDate? {
        val rule = event.recurrence
        if (rule.frequency == EventFrequency.NONE) return null

        // Expande até `after + 1 ano` (suficiente para qualquer frequência
        // anual; para freq. mais curtas o bloco é o próprio dia seguinte).
        val horizon = after.plusYears(1)
        val occ = occurrences(event, after.plusDays(1), horizon)
        return occ.firstOrNull { it > after }
    }

    /** Alinha [start] (data da semana) ao dia da semana [d] da mesma semana. */
    private fun alignToDay(start: LocalDate, d: DayOfWeek): LocalDate =
        start.plusDays(((d.value - start.dayOfWeek.value + 7) % 7).toLong())

    /** Clamp dia 29–31 em meses curtos / 29-fev para o fim do mês. */
    private fun clampMonthDay(date: LocalDate): LocalDate {
        val month = YearMonth.from(date)
        return if (date.dayOfMonth > month.lengthOfMonth()) {
            month.atEndOfMonth()
        } else {
            date
        }
    }

    /** Clamp 29-fev em anos não bissextos para 28-fev. */
    private fun clampYearDay(date: LocalDate): LocalDate {
        if (date.monthValue == 2 && date.dayOfMonth == 29) {
            val month = YearMonth.from(date)
            if (!month.isLeapYear) return date.minusDays(1)
        }
        return date
    }

    /**
     * Data da [position] no [month] (3.ª sexta / último domingo), ou null se a
     * posição não existir nesse mês.
     */
    private fun nthOrLast(month: YearMonth, position: MonthPosition): LocalDate? {
        val dow = position.dayOfWeek.value
        val first = month.atDay(1)
        val offset = ((dow - first.dayOfWeek.value + 7) % 7).toLong()
        val firstMatch = first.plusDays(offset)
        return if (position.ordinal > 0) {
            val candidate = firstMatch.plusWeeks((position.ordinal - 1).toLong())
            candidate.takeIf { YearMonth.from(candidate) == month }
        } else {
            // Último: recua a partir do fim do mês até ao dia da semana.
            val last = month.atEndOfMonth()
            last.minusDays(((last.dayOfWeek.value - dow + 7) % 7).toLong())
        }
    }

    /** Limite inclusivo da série. [OnDate] corta depois dessa data. */
    private fun RecurrenceEnd.inclusiveLimit(): LocalDate = when (this) {
        is RecurrenceEnd.Never -> LocalDate.MAX
        is RecurrenceEnd.OnDate -> date
        is RecurrenceEnd.AfterCount -> LocalDate.MAX
    }
}
