package com.example.view.control.calendar;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.control.calendar.TodoEventInterface;
import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventRepository;
import com.example.view.model.repository.TodoDatabaseHelper;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventViewModel extends ViewModel implements com.example.view.control.calendar.TodoEventInterface {
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final EventRepository eventRepository;
    private final TodoDatabaseHelper todoDatabaseHelper;

    public EventViewModel(Context context) {
        eventRepository = new EventRepository();
        todoDatabaseHelper = new TodoDatabaseHelper(context);
        eventsLiveData.setValue(new ArrayList<>());
        Log.d("EventViewModel", "EventViewModel initialized");
    }

    @Override
    public LiveData<List<Event>> getEventsLiveData() { // Ensure method signature matches the interface
        return eventsLiveData;
    }

    @Override
    public void loadEventsForDate(LocalDateTime date) { // Ensure method signature matches the interface
        try {
            List<Event> eventsForDate = eventRepository.getEventsForDate(date);
            Log.d("EventViewModel", "Requested date: " + date + ", Events found: " + eventsForDate.size());
            eventsLiveData.setValue(eventsForDate);
        } catch (Exception e) {
            Log.e("EventViewModel", "Error loading events for date: " + date, e);
        }
    }

    @Override
    public List<Task> getTasks() { // Ensure method signature matches the interface
        try {
            List<Task> tasks = todoDatabaseHelper.getAllTasksSortedByPriority();
            Log.d("EventViewModel", "Tasks fetched successfully: " + tasks.size());
            return tasks;
        } catch (Exception e) {
            Log.e("EventViewModel", "Error fetching tasks", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteAllTasks() { // Ensure method signature matches the interface
        try {
            todoDatabaseHelper.deleteAllTasks();
            Log.d("EventViewModel", "All tasks deleted successfully");
        } catch (Exception e) {
            Log.e("EventViewModel", "Error deleting tasks", e);
        }
    }

    @Override
    public void createEventFromTask(Task task) { // Ensure method signature matches the interface
        try {
            if (task == null) {
                Log.e("EventViewModel", "Task is null. Cannot create event");
                return;
            }

            Event event = new Event(
                    task.getId(),
                    task.getTask(),
                    task.getCategory().getName(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(1),
                    task.getCategory().getTravelTime(),
                    task.getCategory().getLocation(),
                    null, // No repetition by default
                    task.getDescription(),
                    new ArrayList<>()
            );

            addEvent(event);
            Log.d("EventViewModel", "Event created successfully from task: " + task.getTask());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error creating event from task", e);
        }
    }

    @Override
    public void addEvent(Event event) { // Ensure method signature matches the interface
        try {
            if (event == null) {
                Log.e("EventViewModel", "Event is null. Cannot add to repository");
                return;
            }

            eventRepository.insertEvent(event);
            loadEventsForDate(event.getStartDateTime());
            Log.d("EventViewModel", "Event added successfully: " + event.getTitle());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error adding event", e);
        }
    }

    @Override
    public void deleteEvent(String eventId) { // Ensure method signature matches the interface
        if (eventId == null || eventId.isEmpty()) {
            Log.e("EventViewModel", "Invalid event ID. Cannot delete event");
            return;
        }

        try {
            eventRepository.deleteEvent(eventId);
            Log.d("EventViewModel", "Event deleted successfully with ID: " + eventId);
        } catch (Exception e) {
            Log.e("EventViewModel", "Error deleting event with ID: " + eventId, e);
        }
    }
}
