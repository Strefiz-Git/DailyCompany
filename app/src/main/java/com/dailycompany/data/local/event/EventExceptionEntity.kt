package com.dailycompany.data.local.event

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Exceção de uma **única ocorrência** de um evento — a única coisa relacionada
 * com ocorrências que a BD pode (e deve) guardar (R6): um "anular/editar um dia
 * específico". A ocorrência em si **nunca** é gravada; a exceção referencia-a
 * por [originalDate].
 *
 * Colunas alinhadas com `PROJECT.md` §10 (`EventExceptionEntity`):
 * `id, event_uid, original_date, is_cancelled?, override_date?, override_time?`.
 */
@Entity(tableName = "event_exceptions")
data class EventExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "event_uid") val eventUid: String,
    @ColumnInfo(name = "original_date") val originalDate: LocalDate,
    @ColumnInfo(name = "is_cancelled") val isCancelled: Boolean = false,
    @ColumnInfo(name = "override_date") val overrideDate: LocalDate? = null,
    @ColumnInfo(name = "override_time") val overrideTime: LocalTime? = null,
)
