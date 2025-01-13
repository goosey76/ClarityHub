package com.example.view.control.calendar;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class EventViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public EventViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventViewModel.class)) {
            return (T) new EventViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
