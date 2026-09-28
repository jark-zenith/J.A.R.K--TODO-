package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository

class JarkApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
