package com.example.view.control.todo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.view.control.todo.TodoViewModel;
import com.example.view.model.repository.TodoDatabaseHelper;

public class TodoViewModelFactory implements ViewModelProvider.Factory {
    private final TodoDatabaseHelper todoDatabaseHelper;

    public TodoViewModelFactory(TodoDatabaseHelper todoDatabaseHelper) {
        this.todoDatabaseHelper = todoDatabaseHelper;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TodoViewModel.class)) {
            return (T) new TodoViewModel(todoDatabaseHelper); // Pass dependency here
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

