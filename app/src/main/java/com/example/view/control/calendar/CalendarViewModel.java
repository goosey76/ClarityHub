package com.example.view.control.calendar;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarViewModel extends ViewModel {
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final EventRepository eventRepository;

    public CalendarViewModel() {
        eventRepository = new EventRepository();
        eventsLiveData.setValue(new ArrayList<>());
    }

    public LiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }

    public void loadEventsForDate(LocalDateTime date) {
        List<Event> eventsForDate = eventRepository.getEventsForDate(date);
        Log.d("EventRepository", "Requested date: " + date + ", Events found: " + eventsForDate.size());
        eventsLiveData.setValue(eventsForDate);
    }

    public void addEvent(Event event) {
        eventRepository.insertEvent(event); // Insert into repository
        loadEventsForDate(event.getStartDateTime()); // Refresh LiveData
    }

    public void addEventFromTodo(String todoId, String task, String category, String description, int priority) {
        // Debug log for incoming data
        Log.d("CalendarViewModel", "Creating Event from Todo - Task: " + task + ", Category: " + category);

        // Create an Event from the Todo data
        Event eventFromTodo = new Event(
                todoId, // Use Todo ID as the event ID to link them
                task,
                category,
                LocalDateTime.now(), // Default start time: now
                LocalDateTime.now().plusHours(1), // Default end time: +1 hour
                0, // Default travel time (optional)
                null, // No location specified initially
                null, // No repetition initially
                description,
                new ArrayList<>() // No participants initially
        );

        Log.d("CalendarViewModel", "Event created from Todo: " + eventFromTodo);

        addEvent(eventFromTodo); // Insert the event into the repository and update LiveData
    }

    public void updateEvent(Event eventToEdit) {
        eventRepository.updateEvent(eventToEdit);
    }

    public void deleteEvent(String eventId) {
        if (eventId != null && !eventId.isEmpty()) {
            eventRepository.deleteEvent(eventId);
            Log.d("CalendarViewModel", "Event deleted with ID: " + eventId);
        }
    }

    public String[] getSavedLocations() {
        // Placeholder: Retrieve saved locations from SharedPreferences or database
        return new String[]{"Location 1", "Location 2", "Location 3"};
    }
}
