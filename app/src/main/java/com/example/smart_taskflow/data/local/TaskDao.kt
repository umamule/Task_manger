package com.example.smart_taskflow.data.local

import androidx.room.*
import com.example.smart_taskflow.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY id DESC")
    fun getTasks(userId: Int): Flow<List<Task>>
}
