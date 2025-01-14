package com.example.view;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventDatabaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

public class EventDatabaseHelperTest {

    private EventDatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new EventDatabaseHelper(context);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Clean up the database
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void testInsertEvent() {
        Event event = createSampleEvent("1", "Meeting");

        dbHelper.insertEvent(event);

        List<Event> events = dbHelper.getAllEvents();
        assertEquals(1, events.size());
        assertEquals("Meeting", events.get(0).getTitle());
    }

    @Test
    public void testGetAllEvents() {
        Event event1 = createSampleEvent("1", "Event 1");
        Event event2 = createSampleEvent("2", "Event 2");

        dbHelper.insertEvent(event1);
        dbHelper.insertEvent(event2);

        List<Event> events = dbHelper.getAllEvents();
        assertEquals(2, events.size());
    }

    @Test
    public void testDeleteEventById() {
        Event event = createSampleEvent("1", "Meeting");

        dbHelper.insertEvent(event);
        dbHelper.deleteEventById("1");

        List<Event> events = dbHelper.getAllEvents();
        assertTrue(events.isEmpty());
    }

    @Test
    public void testUpdateEvent() {
        Event event = createSampleEvent("1", "Meeting");
        dbHelper.insertEvent(event);

        // Update the event
        event.setTitle("Updated Meeting");
        dbHelper.updateEvent(event);

        List<Event> events = dbHelper.getAllEvents();
        assertEquals(1, events.size());
        assertEquals("Updated Meeting", events.get(0).getTitle());
    }

    private Event createSampleEvent(String id, String title) {
        return new Event(
                id,
                title,
                "Work",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                30,
                "Berlin",
                "Weekly",
                "Description of the event",
                List.of("John Doe", "Jane Doe")
        );
    }
}