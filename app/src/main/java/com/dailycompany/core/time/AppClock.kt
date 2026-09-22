package com.dailycompany.core.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Fonte de tempo da app — injetado via Hilt (regra: nada de
 * `System.currentTimeMillis()`/`LocalDate.now()` espalhado). Substituível
 * por [FixedClock] nos testes para resultados determinísticos.
 */
interface AppClock {
    fun today(): LocalDate
    fun now(): LocalDateTime
    fun zone(): ZoneId
}

/** Relógio real — usa o fuso do dispositivo. */
class SystemClock(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : AppClock {
    override fun today(): LocalDate = LocalDate.now(zoneId)
    override fun now(): LocalDateTime = LocalDateTime.now(zoneId)
    override fun zone(): ZoneId = zoneId
}

/** Relógio fixo — só para testes. */
class FixedClock(
    private val date: LocalDate,
    private val zoneId: ZoneId = ZoneId.of("Europe/Lisbon"),
    private val time: LocalDateTime = date.atStartOfDay(),
) : AppClock {
    override fun today(): LocalDate = date
    override fun now(): LocalDateTime = time
    override fun zone(): ZoneId = zoneId
}
