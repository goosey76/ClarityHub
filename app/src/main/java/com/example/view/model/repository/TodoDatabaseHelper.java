package com.example.view.model.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 10;

    // Table names
    private static final String TABLE_TODOS = "todos";
    private static final String TABLE_EVENTS = "events";

    // Columns for the Todo table
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TASK = "task";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRIORITY = "priority";
    private static final String COLUMN_EVENT_ID = "event_id";

    // Columns for the Event table
    private static final String COLUMN_EVENT_TITLE = "title";
    private static final String COLUMN_EVENT_START_TIME = "start_time";
    private static final String COLUMN_EVENT_END_TIME = "end_time";
    private static final String COLUMN_EVENT_LOCATION = "location";
    private static final String COLUMN_EVENT_TRAVEL_TIME = "travel_time";
    private static final String COLUMN_EVENT_NOTIFICATION = "notification";
    private static final String COLUMN_EVENT_REPETITION = "repetition";
    private static final String COLUMN_EVENT_TODO_ID = "todo_id";

    private final Context context;

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the Todo table
        String createTodoTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TODOS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TASK + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PRIORITY + " INTEGER, " +
                COLUMN_EVENT_ID + " TEXT)";
        db.execSQL(createTodoTable);

        // Create the Event table
        String createEventTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_EVENT_TITLE + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_EVENT_START_TIME + " TEXT, " +
                COLUMN_EVENT_END_TIME + " TEXT, " +
                COLUMN_EVENT_LOCATION + " TEXT, " +
                COLUMN_EVENT_TRAVEL_TIME + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_EVENT_NOTIFICATION + " INTEGER, " +
                COLUMN_EVENT_REPETITION + " TEXT, " +
                COLUMN_EVENT_TODO_ID + " TEXT)";
        db.execSQL(createEventTable);

        Log.d("TodoDatabaseHelper", "Tables created successfully.");
    }


    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_TODOS, null, null);
            db.delete(TABLE_EVENTS, null, null);
            Log.d("TodoDatabaseHelper", "All tasks and events deleted.");
        } catch (Exception e) {
            Log.e("TodoDatabaseHelper", "Error deleting tasks and events", e);
        } finally {
            db.close();
        }
    }

    public void insertTaskWithEvent(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Create a unique event ID
            String eventId = UUID.randomUUID().toString();

            // Insert into Event table
            ContentValues eventValues = new ContentValues();
            eventValues.put(COLUMN_ID, eventId);
            eventValues.put(COLUMN_EVENT_TITLE, task.getTask());
            eventValues.put(COLUMN_CATEGORY, task.getCategory().getName());
            eventValues.put(COLUMN_EVENT_START_TIME, LocalDateTime.now().toString());
            eventValues.put(COLUMN_EVENT_END_TIME, LocalDateTime.now().plusHours(1).toString());
            eventValues.put(COLUMN_EVENT_LOCATION, task.getCategory().getLocation());
            eventValues.put(COLUMN_EVENT_TRAVEL_TIME, task.getCategory().getTravelTime());
            eventValues.put(COLUMN_DESCRIPTION, task.getDescription());
            eventValues.put(COLUMN_EVENT_NOTIFICATION, 1);
            eventValues.put(COLUMN_EVENT_REPETITION, "Keine");
            eventValues.put(COLUMN_EVENT_TODO_ID, task.getId());

            long eventInsertResult = db.insert(TABLE_EVENTS, null, eventValues);
            if (eventInsertResult == -1) {
                Log.e("TodoDatabaseHelper", "Failed to insert event.");
            } else {
                Log.d("TodoDatabaseHelper", "Event inserted successfully.");
            }

            // Insert into Todo table
            ContentValues todoValues = new ContentValues();
            todoValues.put(COLUMN_ID, task.getId());
            todoValues.put(COLUMN_TASK, task.getTask());
            todoValues.put(COLUMN_CATEGORY, task.getCategory().getName());
            todoValues.put(COLUMN_DESCRIPTION, task.getDescription());
            todoValues.put(COLUMN_PRIORITY, task.getPriority().getValue());
            todoValues.put(COLUMN_EVENT_ID, eventId);

            long todoInsertResult = db.insert(TABLE_TODOS, null, todoValues);
            if (todoInsertResult == -1) {
                Log.e("TodoDatabaseHelper", "Failed to insert task.");
            } else {
                Log.d("TodoDatabaseHelper", "Task inserted successfully.");
            }
        } catch (Exception e) {
            Log.e("TodoDatabaseHelper", "Error inserting Task and Event.", e);
        } finally {
            db.close();
        }
    }

    public List<Task> getAllTasksSortedByPriority() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_TODOS + " ORDER BY " + COLUMN_PRIORITY + " ASC", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String taskName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    int priorityValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY));

                    Category category = Category.fromName(categoryName);
                    Priority priority = Priority.fromValue(priorityValue);

                    taskList.add(new Task(id, taskName, category, description, priority));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TodoDatabaseHelper", "Error fetching tasks.", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return taskList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            onCreate(db);
        }
    }
}
