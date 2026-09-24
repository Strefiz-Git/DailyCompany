package com.dailycompany.data.local.event

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO de eventos (Room). **Sem qualquer coluna/query de ocorrências** (R6) —
 * a ocorrência é sempre calculada a partir da regra; nunca gravada.
 */
@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY date ASC, COALESCE(time, '23:59') ASC, id ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE uid = :uid")
    suspend fun getByUid(uid: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: EventEntity): Long

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
