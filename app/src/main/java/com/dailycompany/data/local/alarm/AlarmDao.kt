package com.dailycompany.data.local.alarm

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Acesso a `alarms`. A UI nunca fala com DAOs (regra AGENTS: através do repositório). */
@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY time, label")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun observeById(id: Long): Flow<AlarmEntity?>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Long): AlarmEntity?

    /** Alarmes ativos — usados pelo receiver de arranque para reagendar (boot). */
    @Query("SELECT * FROM alarms WHERE enabled = 1")
    suspend fun getEnabled(): List<AlarmEntity>

    /** Insere ou atualiza; devolve o id (novo se o alarme ainda não tinha). */
    @Upsert
    suspend fun upsert(alarm: AlarmEntity): Long

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)
}