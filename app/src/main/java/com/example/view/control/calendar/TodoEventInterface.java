package com.example.view.control.calendar;

import androidx.lifecycle.LiveData;
import com.example.view.model.calendar.Event;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoEventInterface {
    LiveData<List<Event>> getEventsLiveData(); // Ensure LiveData is imported
    void loadEventsForDate(LocalDateTime date);
    List<Task> getTasks();
    void deleteAllTasks();
    void createEventFromTask(Task task);
    void addEvent(Event event);
    void deleteEvent(String eventId);
}
