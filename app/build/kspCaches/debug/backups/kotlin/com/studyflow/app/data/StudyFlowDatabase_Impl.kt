package com.studyflow.app.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StudyFlowDatabase_Impl : StudyFlowDatabase() {
  private val _taskDao: Lazy<TaskDao> = lazy {
    TaskDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "802f0e0a5eab88034b18b485def8e228", "6a5967af0f719248b0b4684169bcc888") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `offline_tasks` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `subject` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `priority` TEXT NOT NULL, `completed` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '802f0e0a5eab88034b18b485def8e228')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `offline_tasks`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsOfflineTasks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOfflineTasks.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("description", TableInfo.Column("description", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("subject", TableInfo.Column("subject", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("dueDate", TableInfo.Column("dueDate", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("priority", TableInfo.Column("priority", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("completed", TableInfo.Column("completed", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineTasks.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOfflineTasks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesOfflineTasks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoOfflineTasks: TableInfo = TableInfo("offline_tasks", _columnsOfflineTasks,
            _foreignKeysOfflineTasks, _indicesOfflineTasks)
        val _existingOfflineTasks: TableInfo = read(connection, "offline_tasks")
        if (!_infoOfflineTasks.equals(_existingOfflineTasks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |offline_tasks(com.studyflow.app.data.TaskEntity).
              | Expected:
              |""".trimMargin() + _infoOfflineTasks + """
              |
              | Found:
              |""".trimMargin() + _existingOfflineTasks)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "offline_tasks")
  }

  public override fun clearAllTables() {
    super.performClear(false, "offline_tasks")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(TaskDao::class, TaskDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun taskDao(): TaskDao = _taskDao.value
}
