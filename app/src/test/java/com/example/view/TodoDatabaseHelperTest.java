package com.example.view;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.view.model.repository.TodoDatabaseHelper;
import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TodoDatabaseHelperTest {

    private TodoDatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new TodoDatabaseHelper(context);
        dbHelper.deleteAllTasks(); // Datenbank vor jedem Test bereinigen
    }

    @After
    public void tearDown() {
        dbHelper.deleteAllTasks(); // Datenbank nach jedem Test bereinigen
        dbHelper.close();
    }

    @Test
    public void testInsertTask() {
        Task task = new Task("1", "Testaufgabe", Category.WORK, "Beschreibung", Priority.URGENT_IMPORTANT);

        dbHelper.insertTask(task);

        List<Task> tasks = dbHelper.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Testaufgabe", tasks.get(0).getTask());
    }

    @Test
    public void testGetAllTasks() {
        Task task1 = new Task("1", "Aufgabe 1", Category.UNIVERSITY, "Beschreibung 1", Priority.NOT_URGENT_IMPORTANT);
        Task task2 = new Task("2", "Aufgabe 2", Category.HOUSEHOLD, "Beschreibung 2", Priority.URGENT_NOT_IMPORTANT);

        dbHelper.insertTask(task1);
        dbHelper.insertTask(task2);

        List<Task> tasks = dbHelper.getAllTasks();
        assertEquals(2, tasks.size());
    }

    @Test
    public void testDeleteAllTasks() {
        Task task = new Task("1", "Testaufgabe", Category.WORK, "Beschreibung", Priority.URGENT_IMPORTANT);

        dbHelper.insertTask(task);
        dbHelper.deleteAllTasks();

        List<Task> tasks = dbHelper.getAllTasks();
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void testDeleteTaskById() {
        Task task1 = new Task("1", "Aufgabe 1", Category.WORK, "Beschreibung 1", Priority.URGENT_IMPORTANT);
        Task task2 = new Task("2", "Aufgabe 2", Category.UNIVERSITY, "Beschreibung 2", Priority.NOT_URGENT_IMPORTANT);

        dbHelper.insertTask(task1);
        dbHelper.insertTask(task2);

        dbHelper.deleteTaskById("1");

        List<Task> tasks = dbHelper.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Aufgabe 2", tasks.get(0).getTask());
    }
}
