package com.dailycompany.feature.events

import com.dailycompany.data.repository.EventRepository
import com.dailycompany.domain.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositório de eventos em memória para testes determinísticos (sem Room).
 * Reemite o estado corrente sempre que algo muda (como em `FakeTaskRepository`).
 */
class FakeEventRepository(initial: List<Event> = emptyList()) : EventRepository {

    private val _events = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeAll(): Flow<List<Event>> = _events.asStateFlow()

    override suspend fun getById(id: Long): Event? = _events.value.firstOrNull { it.id == id }

    override suspend fun save(event: Event): Long {
        val id = if (event.id == 0L) nextId++ else event.id
        _events.value = _events.value.filterNot { it.id == id } + event.copy(id = id)
        return id
    }

    override suspend fun deleteById(id: Long) {
        _events.value = _events.value.filterNot { it.id == id }
    }
}