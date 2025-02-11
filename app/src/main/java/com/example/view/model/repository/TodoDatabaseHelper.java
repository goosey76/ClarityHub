package com.example.view.model.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.view.control.cloud.MissingUUIDException;
import com.example.view.control.cloud.RestApiService;
import com.example.view.model.todo.Task;
import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;

import java.util.ArrayList;
import java.util.List;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todos.db";
    private static final int DATABASE_VERSION = 3;

    // Table name
    private static final String TABLE_TODOS = "todos";

    // Columns
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_PRIORITY = "priority";

    private Context context;

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_TODOS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TASK_TITLE + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PRIORITY + " INTEGER)";
        db.execSQL(createTableQuery);
        Log.d("TodoDatabaseHelper", "Todos table created successfully.");
    }

    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, task.getId());
            values.put(COLUMN_TASK_TITLE, task.getTask());
            values.put(COLUMN_CATEGORY, task.getCategory() != null ? task.getCategory().getName() : null);
            values.put(COLUMN_DESCRIPTION, task.getDescription());
            values.put(COLUMN_PRIORITY, task.getPriority().getValue());

            long result = db.insert(TABLE_TODOS, null, values);
            if (result == -1) {
                Log.e("TodoDatabaseHelper", "Failed to insert task.");
            } else {
                Log.d("TodoDatabaseHelper", "Task inserted successfully: " + task.getTask());
                try {
                    //Speichert in Cloud ab
                    RestApiService.sendNewToDo(context, task);
                } catch (MissingUUIDException e) {
                    Log.e("DatabaseHelper", "Error saving task in cloud", e);
                }
            }
        } catch (Exception e) {
            Log.e("TodoDatabaseHelper", "Error inserting task.", e);
        } finally {
            db.close();
        }
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_TODOS, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    int priorityValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY));

                    Category category = categoryName != null ? Category.fromName(categoryName) : null;
                    Priority priority = Priority.fromValue(priorityValue);

                    Task task = new Task(id, title, category, description, priority);
                    taskList.add(task);
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

    /**
     * Löscht alle Aufgaben aus der Datenbank und entfernt sie auch aus der Cloud.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> columnIds = new ArrayList<>();

        try {
            // Abfrage, um alle COLUMN_IDs zu erhalten
            Cursor cursor = db.query(TABLE_TODOS, new String[]{COLUMN_ID}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    columnIds.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                }
                cursor.close();
            }

            // Logge die COLUMN_IDs (optional, zu Debug-Zwecken)
            Log.d("DatabaseHelper", "Saved COLUMN_IDs: " + columnIds);

            // Lösche alle Einträge in der Tabelle
            db.delete(TABLE_TODOS, null, null);
            Log.d("DatabaseHelper", "All tasks deleted");

            new Thread(() -> {
                for (int a = 0; a < columnIds.size(); a++) {
                    // Lösche alle Einträge in der Cloud
                    try {
                        RestApiService.deleteToDoInCloud(context, columnIds.get(a));
                    } catch (MissingUUIDException e) {
                        Log.e("DatabaseHelper", "Error deleting task in cloud", e);
                    }

                    // Timeout von 1 Sekunde
                    try {
                        Thread.sleep(3000); // 1000 Millisekunden = 1 Sekunde
                    } catch (InterruptedException e) {
                        Log.e("Timeout", "Thread wurde unterbrochen", e);
                    }
                }
            }).start();

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting tasks", e);
        } finally {
            db.close();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
            onCreate(db);
        }
    }

    public void deleteTaskById(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsAffected = db.delete(TABLE_TODOS, COLUMN_ID + " = ?", new String[]{taskId});
            if (rowsAffected > 0) {
                Log.d("TodoDatabaseHelper", "Task deleted successfully with ID: " + taskId);
            } else {
                Log.d("TodoDatabaseHelper", "No task found with ID: " + taskId);
            }
        } catch (Exception e) {
            Log.e("TodoDatabaseHelper", "Error deleting task with ID: " + taskId, e);
        } finally {
            db.close();
        }
    }

}
