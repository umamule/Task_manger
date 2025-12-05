package com.example.smart_taskflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.local.AppDatabase
import com.example.smart_taskflow.data.local.TaskRepository
import com.example.smart_taskflow.data.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDB(application)
    private val repo = TaskRepository(db.taskDao())

    var userId: Int = -1

    val tasks = repo.getTasks(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun setUser(id: Int) {
        userId = id
    }

    fun addTask(task: Task) {
        viewModelScope.launch { repo.addTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repo.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repo.deleteTask(task) }
    }
}
