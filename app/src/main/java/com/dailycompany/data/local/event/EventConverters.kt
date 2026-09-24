package com.dailycompany.data.local.event

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/**
 * Converters Room para eventos — java.time guardado como ISO-8601 (String),
 * espelhando `TaskConverters`. A regra de repetição **não** precisa de converter:
 * guarda-se já serializada numa única coluna (`EventEntity.recurrence`, R6).
 */
class EventConverters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}
