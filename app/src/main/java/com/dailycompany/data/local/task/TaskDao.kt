package com.dailycompany.data.local.task

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE done = 0 ORDER BY created_at DESC")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Query("UPDATE tasks SET done = 1, done_at = :doneAt WHERE id = :id")
    suspend fun markDone(id: Long, doneAt: Long)

    @Query("UPDATE tasks SET done = 0, done_at = NULL WHERE id = :id")
    suspend fun markNotDone(id: Long)

    @Query("UPDATE tasks SET due_date = :dueDate WHERE id = :id")
    suspend fun setDueDate(id: Long, dueDate: LocalDate?)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
