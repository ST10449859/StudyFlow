package com.studyflow.app.data

import com.studyflow.app.Task

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        subject = subject,
        dueDate = dueDate,
        priority = priority,
        completed = completed,
        isSynced = true
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        subject = subject,
        dueDate = dueDate,
        priority = priority,
        completed = completed
    )
}
