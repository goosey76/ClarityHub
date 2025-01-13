package com.example.view.control.calendar;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventDatabaseHelper;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventViewModel extends ViewModel implements TodoEventInterface {

    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final EventDatabaseHelper eventDatabaseHelper;

    // Constructor to initialize EventDatabaseHelper
    public EventViewModel(EventDatabaseHelper eventDatabaseHelper) {
        this.eventDatabaseHelper = eventDatabaseHelper;
        eventsLiveData.setValue(new ArrayList<>()); // Initialize LiveData
        loadAllEvents(); // Load all events initially
        Log.d("EventViewModel", "EventViewModel initialized");
    }

    @Override
    public LiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }

    // Load all events from the database and update LiveData
    public void loadAllEvents() {
        try {
            List<Event> allEvents = eventDatabaseHelper.getAllEvents();
            eventsLiveData.setValue(allEvents);
            Log.d("EventViewModel", "All events loaded successfully. Count: " + allEvents.size());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error loading all events", e);
        }
    }

    @Override
    public void createEventFromTaskForDay(Task task, LocalDateTime date) {
        if (task == null || date == null) {
            Log.e("EventViewModel", "Task or date is null. Cannot create event.");
            return;
        }

        // Default start and end times for the event
        LocalDateTime startTime = date.withHour(9).withMinute(0); // 9 AM
        LocalDateTime endTime = startTime.plusHours(1); // Default duration: 1 hour

        // Create an event using task attributes
        Event eventFromTask = new Event(
                task.getId(), // Use the task ID as the event ID
                task.getTitle(),
                "Task", // Default category
                startTime,
                endTime,
                0, // Default travel time
                null, // No location initially
                null, // No repetition
                task.getDescription(), // Use task description as event notes
                new ArrayList<>() // No participants initially
        );

        // Add the event
        addEvent(eventFromTask);
        Log.d("EventViewModel", "Event created from task: " + eventFromTask);
    }

    @Override
    public List<Event> getEventsForTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            Log.e("EventViewModel", "Task ID is null or empty. Cannot retrieve events.");
            return Collections.emptyList();
        }

        try {
            List<Event> allEvents = eventDatabaseHelper.getAllEvents();
            List<Event> eventsForTask = new ArrayList<>();

            // Filter events by the task ID
            for (Event event : allEvents) {
                if (taskId.equals(event.getId())) {
                    eventsForTask.add(event);
                }
            }

            Log.d("EventViewModel", "Events found for task ID: " + taskId + ", Count: " + eventsForTask.size());
            return eventsForTask;
        } catch (Exception e) {
            Log.e("EventViewModel", "Error retrieving events for task ID: " + taskId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void loadEventsForDate(LocalDateTime date) {
        try {
            List<Event> allEvents = eventDatabaseHelper.getAllEvents();
            List<Event> eventsForDate = new ArrayList<>();

            // Filter events by the requested date
            for (Event event : allEvents) {
                if (isSameDay(event.getStartDateTime(), date)) {
                    eventsForDate.add(event);
                }
            }

            eventsLiveData.setValue(eventsForDate);
            Log.d("EventViewModel", "Events loaded for date: " + date + ", Count: " + eventsForDate.size());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error loading events for date: " + date, e);
        }
    }

    @Override
    public void addEvent(Event event) {
        if (event == null) {
            Log.e("EventViewModel", "Cannot add null event.");
            return;
        }

        try {
            eventDatabaseHelper.insertEvent(event); // Save to database
            loadAllEvents(); // Refresh all events
            Log.d("EventViewModel", "Event added successfully: " + event.getTitle());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error adding event: " + event.getTitle(), e);
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("EventViewModel", "Invalid event ID. Cannot delete event.");
            return;
        }

        try {
            eventDatabaseHelper.deleteEventById(eventId); // Delete from database
            loadAllEvents(); // Refresh all events
            Log.d("EventViewModel", "Event deleted successfully with ID: " + eventId);
        } catch (Exception e) {
            Log.e("EventViewModel", "Error deleting event with ID: " + eventId, e);
        }
    }

    @Override
    public void updateEvent(Event event) {
        if (event == null) {
            Log.e("EventViewModel", "Cannot update null event.");
            return;
        }

        try {
            // Call the correct method to update the event
            eventDatabaseHelper.updateEvent(event);

            // Refresh events for the updated event's date
            loadEventsForDate(event.getStartDateTime());
            Log.d("EventViewModel", "Event updated successfully: " + event.getTitle());
        } catch (Exception e) {
            Log.e("EventViewModel", "Error updating event: " + event.getTitle(), e);
        }
    }



    // Helper method to check if two LocalDateTime objects are on the same day
    private boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }
}
