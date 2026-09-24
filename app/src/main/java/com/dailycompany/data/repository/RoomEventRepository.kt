package com.dailycompany.data.repository

import com.dailycompany.core.time.AppClock
import com.dailycompany.data.local.event.EventDao
import com.dailycompany.data.local.event.EventEntity
import com.dailycompany.domain.model.Event
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementação Room de [EventRepository], espelhando `RoomTaskRepository`:
 * - `EventEntity` é o único formato persistido; o domínio [Event] é puro.
 * - `recurrence` viaja numa **única coluna serializada** (R6); as ocorrências
 *   nunca são gravadas — expandidas a pedido por `OccurrenceCalculator`.
 * - `uid` é gerado (UUID) para eventos novos; `created_at` como epoch-millis.
 */
@Singleton
class RoomEventRepository @Inject constructor(
    private val dao: EventDao,
    private val clock: AppClock,
) : EventRepository {

    private fun EventEntity.toDomain(): Event = Event(
        id = id,
        uid = uid,
        title = title,
        date = date,
        time = time,
        isAllDay = isAllDay,
        location = location,
        notes = notes,
        colorKey = colorKey,
        recurrence = recurrenceRule(),
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), clock.zone()),
    )

    private fun Event.toEntity(): EventEntity = EventEntity(
        id = id,
        uid = uid.ifBlank { UUID.randomUUID().toString() },
        title = title,
        date = date,
        time = time,
        isAllDay = isAllDay,
        location = location,
        notes = notes,
        colorKey = colorKey,
        recurrence = recurrence.serialize(),
        createdAt = createdAt.atZone(clock.zone()).toInstant().toEpochMilli(),
    )

    override fun observeAll(): Flow<List<Event>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Event? = dao.getById(id)?.toDomain()

    override suspend fun save(event: Event): Long = dao.upsert(event.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}