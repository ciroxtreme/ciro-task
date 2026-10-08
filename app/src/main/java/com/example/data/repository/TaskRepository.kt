package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.TaskDao
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun getUncompletedTasksWithReminders(): List<TaskEntity> = taskDao.getUncompletedTasksWithReminders()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)
}
