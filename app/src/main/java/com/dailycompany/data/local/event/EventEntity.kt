package com.dailycompany.data.local.event

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dailycompany.domain.model.RecurrenceRule
import com.dailycompany.domain.model.parseRecurrenceRule
import java.time.LocalDate
import java.time.LocalTime

/**
 * Evento persistido (Room). Regra AGENTS: a UI nunca vê `EventEntity` — o
 * mapeamento é feito no repositório. A **ocorrência nunca é gravada** (R6):
 * `recurrence` guarda a regra serializada (ver [serialize]/[parseRecurrenceRule]),
 * e a expansão é sempre calculada por `OccurrenceCalculator`.
 *
 * Colunas alinhadas com `PROJECT.md` §10 (`EventEntity`):
 * `id, uid, title, date, time?, is_all_day, location?, notes?, color_key?,
 * recurrence(regra), created_at`.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val uid: String,
    val title: String,
    /** ISO-8601 (`yyyy-MM-dd`), via [TaskConverters]. */
    val date: LocalDate,
    /** ISO-8601 (`HH:mm`), via [TaskConverters]; nulo = sem hora definida. */
    val time: LocalTime? = null,
    @ColumnInfo(name = "is_all_day") val isAllDay: Boolean = false,
    val location: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "color_key") val colorKey: String? = null,
    /**
     * Regra de repetição serializada — **única coluna** (R6). Predefinição é a
     * regra sem repetição, espelhando `TaskEntity.repeat` (`@ColumnInfo(defaultValue)`).
     */
    @ColumnInfo(name = "recurrence", defaultValue = "NONE|1|||NEVER")
    val recurrence: String = RecurrenceRule().serialize(),
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L,
) {
    /** Regra lida da coluna (nunca bloqueia; malformado ⇒ sem repetição). */
    fun recurrenceRule(): RecurrenceRule = parseRecurrenceRule(recurrence)
}
