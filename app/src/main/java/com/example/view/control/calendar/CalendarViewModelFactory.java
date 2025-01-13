package com.example.view.control.calendar;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CalendarViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public CalendarViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CalendarViewModel.class)) {
            return (T) new ConcreteCalendarViewModel(context);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
