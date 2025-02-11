package com.example.view.model.repository;

import android.content.Context;
import android.util.Log;

import com.example.view.control.cloud.MissingUUIDException;
import com.example.view.control.cloud.RestApiService;
import com.example.view.model.calendar.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventRepository {
    private final EventDatabaseHelper eventDatabaseHelper;

    private Context context;

    public EventRepository(Context context) {
        eventDatabaseHelper = new EventDatabaseHelper(context);
        this.context = context; // Korrekte Zuweisung

    }

    public void insertEvent(Event event) {
        try {
            if (context == null) {
                Log.e("EventRepository", "Context is null. Cannot proceed.");
                return;
            }

            eventDatabaseHelper.insertEvent(event);
            Log.d("EventRepository", "Event inserted: " + event.getTitle());
            Log.d("Cloud", "Event inserted in cloud: " + event.getTitle());
            RestApiService.sendNewEvent(this.context, event);
        } catch (Exception e) {
            Log.e("EventRepository", "Error inserting event", e);
        }
    }

    public void deleteEvent(String eventId) {
        try {
            eventDatabaseHelper.deleteEventById(eventId);
            RestApiService.deleteEventInCloud(context, eventId);
            Log.d("Cloud", "Event deleted in cloud: " + eventId);
            Log.d("EventRepository", "Event deleted with ID: " + eventId);
        } catch (Exception e) {
            Log.e("EventRepository", "Error deleting event", e);
        }
    }

    public void updateEvent(Event event) {
        if (event == null) {
            Log.e("EventRepository", "Cannot update a null event.");
            return;
        }

        eventDatabaseHelper.updateEvent(event); // This should invoke the updated method
        try {
            RestApiService.updateEventInCloud(context, event);
            Log.d("Cloud", "Event updated in cloud: " + event.getTitle());
        } catch (MissingUUIDException e) {
            throw new RuntimeException(e);
        }
        Log.d("EventRepository", "Event updated: " + event.getTitle());
    }


    public List<Event> getEventsForDate(LocalDateTime date) {
        try {
            List<Event> eventsForDate = eventDatabaseHelper.getAllEvents();
            Log.d("EventRepository", "Events found for date: " + date + ", Count: " + eventsForDate.size());
            return eventsForDate;
        } catch (Exception e) {
            Log.e("EventRepository", "Error retrieving events for date", e);
            return null;
        }
    }

    public List<Event> getAllEvents() {
        try {
            return eventDatabaseHelper.getAllEvents();
        } catch (Exception e) {
            Log.e("EventRepository", "Error retrieving all events", e);
            return null;
        }
    }
}
