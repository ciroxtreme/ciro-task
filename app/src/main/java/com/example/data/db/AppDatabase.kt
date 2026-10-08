package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.SubTask
import com.example.data.model.SubTaskConverter
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [TaskEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ciro_task_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }

            private suspend fun populateDefaultData(db: AppDatabase) {
                val categoryDao = db.categoryDao()
                val taskDao = db.taskDao()

                val defaultCategories = listOf(
                    CategoryEntity("default", "Default", "book", "#D6E9FA", 0),
                    CategoryEntity("family", "Family", "burger", "#FCE6CA", 1),
                    CategoryEntity("study", "Study", "backpack", "#FCDADC", 2),
                    CategoryEntity("pet", "Pet", "pet", "#E9DEFC", 3),
                    CategoryEntity("health", "Health", "health", "#D9F4DF", 4)
                )
                categoryDao.insertAll(defaultCategories)

                val cal = Calendar.getInstance()
                val now = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 11)
                cal.set(Calendar.MINUTE, 48)
                val t1 = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 48)
                val t2 = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 13)
                cal.set(Calendar.MINUTE, 48)
                val t3 = cal.timeInMillis

                val starterTasks = listOf(
                    TaskEntity(
                        title = "Today's tasks will be displayed here",
                        remark = "Welcome to Ciro Task! Have a productive day",
                        categoryId = "family",
                        priority = "Medium",
                        tags = "Food,Family",
                        dueTimestamp = t1,
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "An example for simple task",
                        remark = "Quick task note",
                        categoryId = "default",
                        priority = "Low",
                        tags = "Default",
                        dueTimestamp = t2,
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "You can add multiple subtasks to a task",
                        remark = "Check out the subtasks feature below",
                        categoryId = "family",
                        priority = "Medium",
                        tags = "Family",
                        dueTimestamp = t3,
                        subtasksJson = SubTaskConverter.toJson(
                            listOf(
                                SubTask("1", "You can add other sub tasks.", false),
                                SubTask("2", "This is a sub task for more detail.", false)
                            )
                        ),
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "Manage the priority of task",
                        remark = "High priority task test",
                        categoryId = "study",
                        priority = "High",
                        tags = "Study",
                        dueTimestamp = now + (2 * 3600 * 1000),
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "Next 7 days tasks will be displayed here",
                        remark = "Upcoming task example",
                        categoryId = "family",
                        priority = "Low",
                        tags = "Family",
                        dueTimestamp = now + (2 * 86400 * 1000),
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "Use tags make the task more clear",
                        remark = "Tags can have custom cute icons like tree, bag, and tooth",
                        categoryId = "pet",
                        priority = "Medium",
                        tags = "Tree,Bag,Tooth",
                        dueTimestamp = now + (3 * 86400 * 1000),
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "An example of the full feature display",
                        remark = "Including sub tasks, recurring priority tags and planing time.",
                        categoryId = "study",
                        priority = "High",
                        tags = "Sport,Tooth,Design",
                        isRecurring = true,
                        recurringType = "daily",
                        dueTimestamp = now + (4 * 86400 * 1000),
                        subtasksJson = SubTaskConverter.toJson(
                            listOf(
                                SubTask("1", "This is a sub task for more detail.", false),
                                SubTask("2", "You can add other sub tasks.", false)
                            )
                        ),
                        hasReminder = true
                    ),
                    TaskEntity(
                        title = "Plan week goals & extract APK",
                        remark = "First task marked completed",
                        categoryId = "default",
                        priority = "Low",
                        isCompleted = true,
                        completedAt = now - 3600000,
                        dueTimestamp = now - 7200000
                    )
                )

                for (task in starterTasks) {
                    taskDao.insertTask(task)
                }
            }
        }
    }
}
