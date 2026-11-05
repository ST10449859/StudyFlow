package com.studyflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyFlowDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: StudyFlowDatabase? = null

        fun getDatabase(context: Context): StudyFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyFlowDatabase::class.java,
                    "studyflow_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
