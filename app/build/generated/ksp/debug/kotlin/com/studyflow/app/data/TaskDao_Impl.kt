package com.studyflow.app.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TaskDao_Impl(
  __db: RoomDatabase,
) : TaskDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTaskEntity: EntityInsertAdapter<TaskEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTaskEntity = object : EntityInsertAdapter<TaskEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `offline_tasks` (`id`,`title`,`description`,`subject`,`dueDate`,`priority`,`completed`,`isSynced`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpDescription)
        }
        statement.bindText(4, entity.subject)
        statement.bindText(5, entity.dueDate)
        statement.bindText(6, entity.priority)
        val _tmp: Int = if (entity.completed) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
      }
    }
  }

  public override suspend fun insertTask(task: TaskEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfTaskEntity.insert(_connection, task)
  }

  public override suspend fun insertTasks(tasks: List<TaskEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfTaskEntity.insert(_connection, tasks)
  }

  public override fun getAllTasks(): Flow<List<TaskEntity>> {
    val _sql: String = "SELECT * FROM offline_tasks ORDER BY dueDate ASC"
    return createFlow(__db, false, arrayOf("offline_tasks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSubject: Int = getColumnIndexOrThrow(_stmt, "subject")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<TaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpSubject: String
          _tmpSubject = _stmt.getText(_columnIndexOfSubject)
          val _tmpDueDate: String
          _tmpDueDate = _stmt.getText(_columnIndexOfDueDate)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfCompleted).toInt()
          _tmpCompleted = _tmp != 0
          val _tmpIsSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_1 != 0
          _item =
              TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpSubject,_tmpDueDate,_tmpPriority,_tmpCompleted,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedTasks(): List<TaskEntity> {
    val _sql: String = "SELECT * FROM offline_tasks WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSubject: Int = getColumnIndexOrThrow(_stmt, "subject")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<TaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpSubject: String
          _tmpSubject = _stmt.getText(_columnIndexOfSubject)
          val _tmpDueDate: String
          _tmpDueDate = _stmt.getText(_columnIndexOfDueDate)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfCompleted).toInt()
          _tmpCompleted = _tmp != 0
          val _tmpIsSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_1 != 0
          _item =
              TaskEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpSubject,_tmpDueDate,_tmpPriority,_tmpCompleted,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTaskStatus(taskId: String, completed: Boolean) {
    val _sql: String = "UPDATE offline_tasks SET completed = ?, isSynced = 0 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, taskId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTask(taskId: String) {
    val _sql: String = "DELETE FROM offline_tasks WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, taskId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markTaskSynced(taskId: String) {
    val _sql: String = "UPDATE offline_tasks SET isSynced = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, taskId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
