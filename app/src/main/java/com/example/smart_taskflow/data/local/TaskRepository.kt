package com.example.smart_taskflow.data.local

import com.example.smart_taskflow.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getTasks(userId: Int): Flow<List<Task>> {
        return taskDao.getTasks(userId)
    }

    suspend fun addTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}
