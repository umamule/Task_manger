package com.example.smart_taskflow.data.local

import android.content.Context
import androidx.room.*
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.data.model.User

@Database(
    entities = [User::class, Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDB(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskflow_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
