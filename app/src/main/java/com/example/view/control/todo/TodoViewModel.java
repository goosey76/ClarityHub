package com.example.view.control.todo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.model.repository.TodoDatabaseHelper;
import com.example.view.model.todo.Task;

import java.util.List;

public class TodoViewModel extends ViewModel {
    private final TodoDatabaseHelper todoDatabaseHelper;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();


    public TodoViewModel(TodoDatabaseHelper todoDatabaseHelper) {
        this.todoDatabaseHelper = todoDatabaseHelper;
        loadAllTasks(); // Load tasks initially
    }

    public LiveData<List<Task>> getTasksLiveData() {
        return tasksLiveData;
    }

    public void loadAllTasks() {
        List<Task> tasks = todoDatabaseHelper.getAllTasks(); // Fetch tasks from database
        tasksLiveData.setValue(tasks); // Update LiveData
    }

    public void addTask(Task task) {
        todoDatabaseHelper.insertTask(task); // Insert task into database
        loadAllTasks(); // Refresh task list
    }

    public void deleteAllTasks() {
        todoDatabaseHelper.deleteAllTasks(); // Delete all tasks from database
        loadAllTasks(); // Refresh task list
    }
}

