package com.example.view.control.calendar;

import androidx.lifecycle.LiveData;

import com.example.view.model.calendar.Event;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for managing events and integrating tasks into the calendar system.
 */
public interface TodoEventInterface {

    /**
     * Retrieves the LiveData object for the list of events.
     *
     * @return LiveData object containing the list of events.
     */
    LiveData<List<Event>> getEventsLiveData();

    /**
     * Loads the events for the specified date.
     *
     * @param date The date to load events for.
     */
    void loadEventsForDate(LocalDateTime date);

    /**
     * Adds a new event to the repository.
     *
     * @param event The event to add.
     */
    void addEvent(Event event);

    /**
     * Deletes an event by its ID.
     *
     * @param eventId The ID of the event to delete.
     */
    void deleteEvent(String eventId);

    /**
     * Updates an existing event in the repository.
     *
     * @param event The event to update.
     */
    void updateEvent(Event event);

    /**
     * Loads all events from the repository.
     */
    void loadAllEvents();

    /**
     * Creates an event based on the attributes of a task for the specified day.
     *
     * @param task The task to convert into an event.
     * @param date The date for the event (defaults to the day of the task).
     */
    void createEventFromTaskForDay(Task task, LocalDateTime date);

    /**
     * Fetches the events for a specific task, allowing the user to track
     * the connection between tasks and related events.
     *
     * @param taskId The ID of the task for which to fetch related events.
     * @return A list of events associated with the task.
     */
    List<Event> getEventsForTask(String taskId);
}
