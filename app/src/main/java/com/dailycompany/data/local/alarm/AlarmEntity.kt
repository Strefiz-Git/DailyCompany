package com.dailycompany.data.local.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Alarme persistido. Espelha o domínio [com.dailycompany.domain.model.Alarm]:
 * - `time` guardado como `LocalTime` (conversor `TaskConverters`, texto `HH:mm`);
 * - `repeat_days` numa única coluna serializada (`|` entre nomes, vazio = 1 vez);
 * - `uid` estável (UUID gerado no repositório) para futuras restauras do backup.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val uid: String,
    val time: LocalTime,
    val repeat_days: String,
    val label: String,
    val sound_enabled: Boolean,
    val vibrate_enabled: Boolean,
    val auto_snooze_minutes: Int,
    val enabled: Boolean,
    val created_at: Long,
)