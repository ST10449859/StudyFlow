package com.studyflow.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val description: String?,
    val subject: String,
    val dueDate: String,
    val priority: String,
    val completed: Boolean,

    // Tracks whether this task needs uploading to the API
    val isSynced: Boolean = false
)
