package com.gongfu.taskmanager

import java.io.File
import kotlin.io.forEachLine

// defining what essential properties are in every task
data class Task(
    val title: String,
    val description: String,
    var status: String = "Not Done"
)


// methods and properties responsible for task management
class TaskManager {
    val taskList = mutableListOf<Task>()

    fun loadTaskList(filename: String): Boolean {
        return try {
            val file = File("${System.getProperty("user.dir")}/$filename")
            if (!file.exists()) {
                println("File not found: $filename")
                return false
            }

            taskList.clear()
            file.forEachLine { line ->
                val parts = line.split('|')
                if (parts.size == 3) {
                    val task = Task(parts[0].trim(), parts[1].trim(), parts[2].trim())
                    taskList.add(task)
                }
            }

            println("Loaded ${taskList.size} tasks from $filename")
            return true
        } catch (e: Exception) {
            println("Load failed: ${e.message}")
            false
        }
   }
    fun addTask(task: Task) {
        taskList.add(task)
    }

    fun listTasks() {
        if (taskList.isNotEmpty()) {
            println("\nTasks:")
            for ((index, task) in taskList.withIndex()) {
                println(
                    "${index + 1}. ${task.title} - " +
                            "${task.description} - ${task.status}"
                )
            }
        } else
            println("Task list is empty.")
    }

    fun markTaskAsDone(taskIndex: Int) {
        if (taskIndex in taskList.indices) {
            taskList[taskIndex].status = "Done"
        } else {
            println("Invalid task index. Task not found.")
        }
    }

    fun deleteTask(taskIndex: Int) {
        if (taskIndex in taskList.indices) {
            taskList.removeAt(taskIndex)
        } else {
            println("Invalid task index. Task not found.")
        }
    }

    fun saveTaskList(): Boolean {
        return try {
            val safeFileNameRegex = Regex("""^[A-Za-z0-9 _.-]{1,255}$""")
            var input: String

            while (true) {
                println("Please enter a name for the task list:")
                input = readln().trim()

                if (input.isEmpty() || !input.matches(safeFileNameRegex)) {
                    println("Invalid Filename.")
                    continue
                }
                break
            }

            val filename = "$input.txt"
            val file = File("${System.getProperty("user.dir")}/$filename")
            file.parentFile?.mkdirs()

            file.bufferedWriter().use { writer ->
                taskList.joinTo(writer, "\n") { "${it.title}|${it.description}|${it.status}" }
            }
            println("Task list saved/updated as $filename")
            true
        } catch (e: Exception) {
            println("Save failed: ${e.message}")
            false
        }
    }
}


fun printOptions() {
    println("\nTask Manager Menu:")
    println("1. Add Tasks")
    println("2. List Tasks")
    println("3. Mark Task as Done")
    println("4. Delete Task")
    println("5. Exit")
    println("6. Load Tasks")
    print("Enter your choice (1-6):")
}

fun readIndex(taskListSize: Int): Int? {
    val input = readln()
    if (input.isBlank()) {
        println("Invalid input. Please enter a valid task number.")
        return null
    }

    val taskNumber = input.toIntOrNull()
    if (taskNumber != null && taskNumber >= 1 &&
        taskNumber <= taskListSize
    ) {
        return taskNumber
    } else {
        println("Invalid task number. Please enter a valid task number.")
        return null
    }
}

fun TaskManager.saveTasksWithPrompt(): Boolean {
    if (taskList.isEmpty()) {
        println("No tasks to save")
        return false
    }
    println("Enter 1 to save. 2 to Discard:")
    return when (readln()) {
        "1" -> {
            saveTaskList()
            true
        }
        "2" -> {
            false
        }
        else -> {
            println("Invalid. Discarding...")
            false
        }
    }
}


fun main() {
    val taskManager = TaskManager()

    while (true) {
        printOptions()
        when (readln()) {
            "1" -> {
                print("\nEnter task title: ")
                val title = readln()
                print("\nEnter task description: ")
                val description = readln()
                val task = Task(title, description)
                taskManager.addTask(task)
            }

            "2" -> {
                taskManager.listTasks()
            }

            "3" -> {
                taskManager.listTasks()
                if (taskManager.taskList.isEmpty()) {
                    continue
                } else {
                    print("\nEnter the task number to mark as done:")
                    val taskNumber =
                        readIndex(taskManager.taskList.size)
                    if (taskNumber != null) {
                        taskManager.markTaskAsDone(taskNumber - 1)
                    }
                }
            }

            "4" -> {
                taskManager.listTasks()
                if (taskManager.taskList.isEmpty()) {
                    continue
                } else {
                    print("\nEnter the task number to be deleted:")
                    val taskNumber = readIndex(taskManager.taskList.size)
                    if (taskNumber != null) {
                        taskManager.deleteTask(taskNumber - 1)
                    }
                }
            }

            "5" -> {
                taskManager.saveTasksWithPrompt()
                break
            }
            "6" -> {
                print("\nEnter the filename to load:")
                val filename = readln().trim() + ".txt"
                taskManager.loadTaskList(filename)
            }

            else -> println("\nInvalid choice, Please try again.")
        }
    }
}