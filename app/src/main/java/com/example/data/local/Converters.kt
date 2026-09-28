package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.value

    @TypeConverter
    fun toStatus(value: String): TaskStatus = TaskStatus.fromString(value)

    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.value

    @TypeConverter
    fun toPriority(value: String): TaskPriority = TaskPriority.fromString(value)

    @TypeConverter
    fun fromStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            if (value.isBlank()) emptyList() else json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
