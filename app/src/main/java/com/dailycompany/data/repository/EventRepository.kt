package com.dailycompany.data.repository

import com.dailycompany.domain.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Fonte de eventos vista pela camada de domínio/UI.
 * A UI nunca fala com `EventDao` (regra AGENTS: dados ⇄ domínio via mapper).
 *
 * As ocorrências de um evento repetido **nunca** são gravadas (R6): quem
 * precisa delas (Agenda, calendário) expande a regra com `OccurrenceCalculator`.
 */
interface EventRepository {
    /** Todos os eventos, por data (depois hora). */
    fun observeAll(): Flow<List<Event>>

    suspend fun getById(id: Long): Event?

    /** Insere ou atualiza; devolve o id (novo se o evento ainda não tinha). */
    suspend fun save(event: Event): Long

    suspend fun deleteById(id: Long)
}