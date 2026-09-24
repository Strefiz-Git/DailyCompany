package com.dailycompany.data.local.event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * DAO de exceções de eventos (Room). Guarda apenas exceções de ocorrências
 * (anular/alterar um dia específico); a ocorrência em si é sempre calculada
 * (R6) — nunca gravada.
 */
@Dao
interface EventExceptionDao {

    @Query("SELECT * FROM event_exceptions WHERE event_uid = :eventUid ORDER BY original_date ASC")
    fun observeForEvent(eventUid: String): Flow<List<EventExceptionEntity>>

    @Query("SELECT * FROM event_exceptions WHERE event_uid = :eventUid AND original_date = :originalDate")
    suspend fun get(eventUid: String, originalDate: LocalDate): EventExceptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exception: EventExceptionEntity): Long

    @Query("DELETE FROM event_exceptions WHERE event_uid = :eventUid AND original_date = :originalDate")
    suspend fun delete(eventUid: String, originalDate: LocalDate)

    @Query("DELETE FROM event_exceptions WHERE event_uid = :eventUid")
    suspend fun deleteForEvent(eventUid: String)
}
