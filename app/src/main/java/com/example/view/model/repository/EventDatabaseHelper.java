package com.example.view.model.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.view.control.cloud.MissingUUIDException;
import com.example.view.control.cloud.RestApiService;
import com.example.view.model.calendar.Event;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 5; // Incremented for updates

    // Table name
    private static final String TABLE_EVENTS = "events";

    // Column names
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_EVENT_TITLE = "title";
    private static final String COLUMN_EVENT_START_TIME = "start_time";
    private static final String COLUMN_EVENT_END_TIME = "end_time";
    private static final String COLUMN_EVENT_LOCATION = "location";
    private static final String COLUMN_EVENT_TRAVEL_TIME = "travel_time";
    private static final String COLUMN_EVENT_REPETITION = "repetition";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_PARTICIPANTS = "participants";
    private Context context;
    public EventDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_EVENT_TITLE + " TEXT NOT NULL, " +
                COLUMN_EVENT_START_TIME + " TEXT NOT NULL, " +
                COLUMN_EVENT_END_TIME + " TEXT NOT NULL, " +
                COLUMN_EVENT_LOCATION + " TEXT, " +
                COLUMN_EVENT_TRAVEL_TIME + " INTEGER, " +
                COLUMN_EVENT_REPETITION + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_PARTICIPANTS + " TEXT)";
        db.execSQL(createEventTable);
        Log.d("EventDatabaseHelper", "Events table created successfully.");
    }

    public void insertEvent(Event event) {
        if (event == null) {
            Log.e("EventDatabaseHelper", "Cannot insert a null event.");
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = getContentValuesFromEvent(event);

            long result = db.insert(TABLE_EVENTS, null, values);
            if (result == -1) {
                Log.e("EventDatabaseHelper", "Failed to insert event: " + event.getId());
            } else {
                Log.d("EventDatabaseHelper", "Event inserted successfully: " + event.getId());

                //Speichert in Cloud ab
                RestApiService.sendNewEvent(context, event);
            }
        } catch (Exception e) {
            Log.e("EventDatabaseHelper", "Error inserting event: " + event.getId(), e);
        } finally {
            db.close();
        }
    }

    public void updateEvent(Event event) {
        if (event == null) {
            Log.e("EventDatabaseHelper", "Cannot update a null event.");
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = getContentValuesFromEvent(event); // Ensure this method is converting Event correctly

            // Update the event in the database
            int rowsAffected = db.update(
                    TABLE_EVENTS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{event.getId()}
            );

            if (rowsAffected > 0) {
                Log.d("EventDatabaseHelper", "Event updated successfully: " + event.getTitle());
                RestApiService.updateEventInCloud(context, event);
            } else {
                Log.w("EventDatabaseHelper", "No event found with ID: " + event.getId() + ". Update failed.");
            }
        } catch (Exception e) {
            Log.e("EventDatabaseHelper", "Error updating event: " + event.getTitle(), e);
        } finally {
            db.close();
        }
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
            if (cursor.moveToFirst()) {
                do {
                    Event event = getEventFromCursor(cursor);
                    eventList.add(event);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("EventDatabaseHelper", "Error fetching events.", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return eventList;
    }

    public void deleteEventById(String eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_EVENTS, COLUMN_ID + " = ?", new String[]{eventId});
            if (rowsDeleted > 0) {
                Log.d("EventDatabaseHelper", "Event deleted successfully with ID: " + eventId);
                RestApiService.deleteEventInCloud(context, eventId);
            } else {
                Log.w("EventDatabaseHelper", "No event found with ID: " + eventId);
            }
        } catch (Exception e) {
            Log.e("EventDatabaseHelper", "Error deleting event with ID: " + eventId, e);
        } finally {
            db.close();
        }
    }

    private ContentValues getContentValuesFromEvent(Event event) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, event.getId());
        values.put(COLUMN_EVENT_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_START_TIME, event.getStartDateTime().toString());
        values.put(COLUMN_EVENT_END_TIME, event.getEndDateTime().toString());
        values.put(COLUMN_EVENT_LOCATION, event.getLocation());
        values.put(COLUMN_EVENT_TRAVEL_TIME, event.getTravelTime());
        values.put(COLUMN_EVENT_REPETITION, event.getRepetition());
        values.put(COLUMN_DESCRIPTION, event.getNotes());
        values.put(COLUMN_CATEGORY, event.getCategory());

        if (event.getParticipants() != null && !event.getParticipants().isEmpty()) {
            JSONArray jsonArray = new JSONArray(event.getParticipants());
            values.put(COLUMN_PARTICIPANTS, jsonArray.toString());
        }
        return values;
    }

    private Event getEventFromCursor(Cursor cursor) throws Exception {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE));
        String startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_TIME));
        String endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_TIME));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION));
        int travelTime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TRAVEL_TIME));
        String repetition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_REPETITION));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));

        List<String> participants = new ArrayList<>();
        String participantsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANTS));
        if (participantsJson != null) {
            JSONArray jsonArray = new JSONArray(participantsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                participants.add(jsonArray.getString(i));
            }
        }

        return new Event(
                id,
                title,
                category,
                LocalDateTime.parse(startTime),
                LocalDateTime.parse(endTime),
                travelTime,
                location,
                repetition,
                description,
                participants
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            onCreate(db);
            Log.d("EventDatabaseHelper", "Database upgraded to version: " + newVersion);
        }
    }
}
