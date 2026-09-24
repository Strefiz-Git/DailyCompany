package com.dailycompany.data.repository

import com.dailycompany.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Fonte de alarmes vista pelo domínio/UI. A UI nunca fala com `AlarmDao`
 * (regra AGENTS: dados ⇄ domínio via mapper).
 *
 * A base de dados é a fonte de verdade; o `AlarmManager` é apenas uma cache
 * de agendamento — quem altera um alarme também (re)agenda ou cancela.
 */
interface AlarmRepository {

    fun observeAll(): Flow<List<Alarm>>

    fun observeById(id: Long): Flow<Alarm?>

    suspend fun getById(id: Long): Alarm?

    /** Alarmes ativos — para reagendar no arranque (boot receiver). */
    suspend fun getEnabled(): List<Alarm>

    /** Insere ou atualiza; devolve o id (novo se o alarme ainda não tinha). */
    suspend fun save(alarm: Alarm): Long

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun deleteById(id: Long)
}