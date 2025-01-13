package com.example.view.ui.todo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.view.R;
import com.example.view.control.todo.TodoViewModel;
import com.example.view.control.todo.TodoViewModelFactory;
import com.example.view.model.repository.TodoDatabaseHelper;
import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private TodoViewModel todoViewModel; // ViewModel for managing tasks
    private int selectedPriority = -1; // Default invalid priority
    private Category selectedCategory; // Selected category from spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize TodoDatabaseHelper
        TodoDatabaseHelper todoDatabaseHelper = new TodoDatabaseHelper(this);

        // Initialize ViewModel with custom factory
        TodoViewModelFactory factory = new TodoViewModelFactory(todoDatabaseHelper);
        todoViewModel = new ViewModelProvider(this, factory).get(TodoViewModel.class);

        // Initialize input fields and button
        EditText editTextTask = findViewById(R.id.editTextTask);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        Button buttonSaveTask = findViewById(R.id.buttonSaveTask);
        Spinner spinnerPriority = findViewById(R.id.spinner_priority);
        Spinner spinnerCategory = findViewById(R.id.spinner_category);

        // Set spinner listener for categories
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = Category.values()[position]; // Match the spinner position to Category enum
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });

        // Set spinner listener for priorities
        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = position + 1; // Store priority value as an integer (1-based index)
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPriority = -1; // Reset to invalid priority
            }
        });

        // Set button click listener to save task
        buttonSaveTask.setOnClickListener(v -> {
            // Read input values
            String taskName = editTextTask.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            // Validate input
            if (!validateInputs(taskName)) {
                return;
            }

            // Convert selectedPriority (int) to Priority enum
            Priority priority = Priority.fromValue(selectedPriority);

            // Generate a unique task ID
            String taskId = UUID.randomUUID().toString();

            // Create a new task object
            Task newTask = new Task(taskId, taskName, selectedCategory, description, priority);

            // Use ViewModel to handle task creation
            saveTask(newTask);

            // Finish the activity and return to the previous screen
            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private boolean validateInputs(String taskName) {
        if (taskName.isEmpty()) {
            Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedPriority == -1) {
            Toast.makeText(this, "Please select a priority", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveTask(Task task) {
        try {
            todoViewModel.addTask(task); // Save task using the TodoViewModel
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
