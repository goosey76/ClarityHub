package com.example.view.model.repository;

import android.util.Log;

import com.example.view.model.calendar.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    // Placeholder for in-memory event storage. Replace this with actual database integration.
    private final List<Event> events = new ArrayList<>();

    /**
     * Inserts an event into the repository.
     *
     * @param event The event to insert.
     */
    public void insertEvent(Event event) {
        try {
            events.add(event);
            Log.d("EventRepository", "Event inserted: " + event.getTitle());
        } catch (Exception e) {
            Log.e("EventRepository", "Error inserting event", e);
        }
    }

    /**
     * Deletes an event from the repository by its ID.
     *
     * @param eventId The ID of the event to delete.
     */
    public void deleteEvent(String eventId) {
        try {
            boolean removed = events.removeIf(event -> event.getId().equals(eventId));
            if (removed) {
                Log.d("EventRepository", "Event deleted with ID: " + eventId);
            } else {
                Log.w("EventRepository", "Event with ID not found: " + eventId);
            }
        } catch (Exception e) {
            Log.e("EventRepository", "Error deleting event", e);
        }
    }

    /**
     * Updates an existing event in the repository.
     *
     * @param event The event to update.
     */
    public void updateEvent(Event event) {
        try {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId().equals(event.getId())) {
                    events.set(i, event);
                    Log.d("EventRepository", "Event updated: " + event.getTitle());
                    return;
                }
            }
            Log.w("EventRepository", "Event not found for update: " + event.getId());
        } catch (Exception e) {
            Log.e("EventRepository", "Error updating event", e);
        }
    }

    /**
     * Retrieves all events for a specific date.
     *
     * @param date The date to retrieve events for.
     * @return A list of events for the specified date.
     */
    public List<Event> getEventsForDate(LocalDateTime date) {
        List<Event> eventsForDate = new ArrayList<>();
        try {
            LocalDate localDate = date.toLocalDate();
            for (Event event : events) {
                LocalDate startDate = event.getStartDateTime().toLocalDate();
                LocalDate endDate = event.getEndDateTime().toLocalDate();

                // Check if the date is within the event's start and end dates
                if ((localDate.isEqual(startDate) || localDate.isAfter(startDate)) &&
                        (localDate.isEqual(endDate) || localDate.isBefore(endDate))) {
                    eventsForDate.add(event);
                }
            }
            Log.d("EventRepository", "Events found for date " + date + ": " + eventsForDate.size());
        } catch (Exception e) {
            Log.e("EventRepository", "Error retrieving events for date", e);
        }
        return eventsForDate;
    }

    /**
     * Retrieves all events in the repository (for debugging or display purposes).
     *
     * @return A list of all events.
     */
    public List<Event> getAllEvents() {
        try {
            return new ArrayList<>(events);
        } catch (Exception e) {
            Log.e("EventRepository", "Error retrieving all events", e);
            return new ArrayList<>();
        }
    }
}
