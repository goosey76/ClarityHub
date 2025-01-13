package com.example.view.control.calendar;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventRepository;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class CalendarViewModel extends ViewModel implements TodoEventInterface {
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final EventRepository eventRepository;

    public CalendarViewModel(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        eventRepository = new EventRepository(context);
        loadAllEvents(); // Load all events initially
    }

    @Override
    public LiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }

    @Override
    public void loadEventsForDate(LocalDateTime date) {
        if (date == null) {
            Log.e("CalendarViewModel", "Date is null. Cannot load events.");
            return;
        }
        try {
            List<Event> eventsForDate = eventRepository.getEventsForDate(date);
            eventsLiveData.setValue(eventsForDate);
            Log.d("CalendarViewModel", "Events loaded for date: " + date + ", count: " + eventsForDate.size());
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error loading events for date: " + date, e);
        }
    }

    @Override
    public void addEvent(Event event) {
        if (event == null) {
            Log.e("CalendarViewModel", "Event is null. Cannot add.");
            return;
        }
        try {
            eventRepository.insertEvent(event);
            loadEventsForDate(event.getStartDateTime());
            Log.d("CalendarViewModel", "Event added: " + event.getTitle());
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error adding event: " + event, e);
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("CalendarViewModel", "Invalid event ID. Cannot delete.");
            return;
        }
        try {
            eventRepository.deleteEvent(eventId);
            loadAllEvents();
            Log.d("CalendarViewModel", "Event deleted with ID: " + eventId);
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error deleting event with ID: " + eventId, e);
        }
    }

    @Override
    public void updateEvent(Event event) {
        if (event == null) {
            Log.e("CalendarViewModel", "Event is null. Cannot update.");
            return;
        }
        try {
            eventRepository.updateEvent(event);
            loadEventsForDate(event.getStartDateTime());
            Log.d("CalendarViewModel", "Event updated: " + event.getTitle());
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error updating event: " + event, e);
        }
    }

    @Override
    public void loadAllEvents() {
        try {
            List<Event> allEvents = eventRepository.getAllEvents();
            eventsLiveData.setValue(allEvents);
            Log.d("CalendarViewModel", "All events loaded, count: " + allEvents.size());
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error loading all events", e);
        }
    }

    @Override
    public void createEventFromTaskForDay(Task task, LocalDateTime date) {
        if (task == null) {
            Log.e("CalendarViewModel", "Task is null. Cannot create an event.");
            return;
        }
        if (date == null) {
            Log.e("CalendarViewModel", "Date is null. Cannot create an event.");
            return;
        }

        try {
            // Default start and end times for the event
            LocalDateTime startTime = date.withHour(9).withMinute(0); // Default start time: 9 AM
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
            Log.d("CalendarViewModel", "Event created from task: " + eventFromTask);
        } catch (Exception e) {
            Log.e("CalendarViewModel", "Error creating event from task", e);
        }
    }
}
