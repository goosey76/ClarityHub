package com.example.view.control.todo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.model.repository.TodoDatabaseHelper;
import com.example.view.model.todo.Task;

import java.util.List;

public class ToDoViewModel extends ViewModel {
    private final TodoDatabaseHelper databaseHelper;
    private final MutableLiveData<List<Task>> tasksLiveData = new MutableLiveData<>();

    public ToDoViewModel(TodoDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public LiveData<List<Task>> getTasksLiveData() {
        return tasksLiveData;
    }

    public void loadTasks() {
        List<Task> tasks = databaseHelper.getAllTasksSortedByPriority();
        tasksLiveData.postValue(tasks);
    }

    public void addTask(Task task) {
        databaseHelper.insertTaskWithEvent(task);
        loadTasks(); // Refresh the task list
    }

    public void deleteAllTasks() {
        databaseHelper.deleteAllTasks();
        loadTasks(); // Refresh the task list
    }
}
