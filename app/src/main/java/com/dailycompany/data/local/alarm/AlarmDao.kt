package com.dailycompany.data.local.alarm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    /**
     * Insere ou substitui; devolve o id (novo se o alarme ainda não tinha).
     * `@Insert(REPLACE)` em vez de `@Upsert`: este auto-gera o id quando é 0
     * (novos alarmes) — `@Upsert` inseriria a literal 0 e os novos alarmes
     * sobrescreviam-se todos na mesma linha.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarm: AlarmEntity): Long

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)
}