package com.dailycompany.data.local.task

import java.time.LocalDate
import java.time.LocalTime

/**
 * Converters para java.time no Room — a base de dados guarda os tipos
 * nativos de `java.time` (minSdk 26 ⇒ sem desugaring).
 */
class TaskConverters {
    @androidx.room.TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @androidx.room.TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @androidx.room.TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @androidx.room.TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @androidx.room.TypeConverter
    fun fromPriority(value: TaskPriorityEntity?): String? = value?.name

    @androidx.room.TypeConverter
    fun toPriority(value: String?): TaskPriorityEntity? = value?.let {
        TaskPriorityEntity.entries.firstOrNull { p -> p.name == it } ?: TaskPriorityEntity.NONE
    }

    @androidx.room.TypeConverter
    fun fromRepeat(value: TaskRepeatEntity?): String? = value?.name

    @androidx.room.TypeConverter
    fun toRepeat(value: String?): TaskRepeatEntity? = value?.let {
        TaskRepeatEntity.entries.firstOrNull { p -> p.name == it } ?: TaskRepeatEntity.NONE
    }
}
