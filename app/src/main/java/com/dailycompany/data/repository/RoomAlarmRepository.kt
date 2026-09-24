package com.dailycompany.data.repository

import com.dailycompany.core.time.AppClock
import com.dailycompany.data.local.alarm.AlarmDao
import com.dailycompany.data.local.alarm.AlarmEntity
import com.dailycompany.domain.model.Alarm
import com.dailycompany.domain.model.parseRepeatDays
import com.dailycompany.domain.model.serializeRepeatDays
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementação Room de [AlarmRepository], espelhando `RoomEventRepository`:
 * - `AlarmEntity` é o único formato persistido; o domínio [Alarm] é puro.
 * - `repeat_days` numa única coluna serializada.
 * - `uid` gerado (UUID) para alarmes novos; `created_at` como epoch-millis.
 */
@Singleton
class RoomAlarmRepository @Inject constructor(
    private val dao: AlarmDao,
    private val clock: AppClock,
) : AlarmRepository {

    private fun AlarmEntity.toDomain(): Alarm = Alarm(
        id = id,
        uid = uid,
        time = time,
        repeatDays = parseRepeatDays(repeat_days),
        label = label,
        soundEnabled = sound_enabled,
        vibrateEnabled = vibrate_enabled,
        autoSnoozeMinutes = auto_snooze_minutes,
        enabled = enabled,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(created_at), clock.zone()),
    )

    private fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
        id = id,
        uid = uid.ifBlank { UUID.randomUUID().toString() },
        time = time,
        repeat_days = serializeRepeatDays(repeatDays),
        label = label,
        sound_enabled = soundEnabled,
        vibrate_enabled = vibrateEnabled,
        auto_snooze_minutes = autoSnoozeMinutes,
        enabled = enabled,
        created_at = createdAt.atZone(clock.zone()).toInstant().toEpochMilli(),
    )

    override fun observeAll(): Flow<List<Alarm>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Alarm?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Alarm? = dao.getById(id)?.toDomain()

    override suspend fun getEnabled(): List<Alarm> = dao.getEnabled().map { it.toDomain() }

    override suspend fun save(alarm: Alarm): Long = dao.upsert(alarm.toEntity())

    override suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}