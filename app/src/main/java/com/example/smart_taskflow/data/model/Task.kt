package com.example.smart_taskflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val dueDate: Date?,
    val isDone: Boolean = false,
    val isImportant: Boolean = false,

    val userId: Int   // <-- Room User ID (not Firebase)
)
