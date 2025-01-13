package com.example.view.ui.todo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.view.R;
import com.example.view.control.calendar.EventViewModel;
import com.example.view.control.calendar.EventViewModelFactory;
import com.example.view.model.todo.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private EventViewModel eventViewModel;

    public TodoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Use the custom factory to initialize EventViewModel
        EventViewModelFactory factory = new EventViewModelFactory(context);
        eventViewModel = new ViewModelProvider(this, factory).get(EventViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), taskList);
        recyclerView.setAdapter(taskAdapter);

        // Add Task Button
        FloatingActionButton fabAddTask = view.findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddTaskActivity.class);
            startActivity(intent);
        });

        // Delete All Button
        Button buttonDeleteAll = view.findViewById(R.id.buttonDeleteAll);
        buttonDeleteAll.setOnClickListener(v -> deleteAllTasks());

        // Sort Button
        ImageView imageViewSort = view.findViewById(R.id.imageViewSort);
        imageViewSort.setOnClickListener(v -> showSortMenu(imageViewSort));

        // Load tasks initially
        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload tasks when returning to this fragment
        loadTasks();
    }

    private void loadTasks() {
        try {
            Log.d("TodoFragment", "Loading tasks...");
            taskList.clear();
            taskList.addAll(eventViewModel.getTasks()); // Fetch tasks from EventViewModel
            taskAdapter.notifyDataSetChanged();
            Log.d("TodoFragment", "Tasks loaded: " + taskList.size());
            for (Task task : taskList) {
                Log.d("TodoFragment", "Loaded Task: " + task.getTask());
            }
        } catch (Exception e) {
            Log.e("TodoFragment", "Error loading tasks", e);
            Toast.makeText(getContext(), "Error loading tasks: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteAllTasks() {
        try {
            Log.d("TodoFragment", "Deleting all tasks...");
            eventViewModel.deleteAllTasks(); // Call the delete method in EventViewModel
            loadTasks(); // Refresh the task list
            Toast.makeText(getContext(), "All tasks deleted", Toast.LENGTH_SHORT).show();
            Log.d("TodoFragment", "All tasks deleted successfully.");
        } catch (Exception e) {
            Log.e("TodoFragment", "Error deleting all tasks", e);
            Toast.makeText(getContext(), "Error deleting tasks: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showSortMenu(ImageView anchor) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.sort_by_priority) {
                sortTasksByPriority();
                return true;
            } else if (itemId == R.id.sort_by_category) {
                sortTasksByCategory();
                return true;
            } else if (itemId == R.id.sort_by_name) {
                sortTasksByName();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void sortTasksByPriority() {
        Collections.sort(taskList, (task1, task2) -> task1.getPriority().getValue() - task2.getPriority().getValue());
        taskAdapter.notifyDataSetChanged();
        Log.d("TodoFragment", "Tasks sorted by priority.");
    }

    private void sortTasksByCategory() {
        Collections.sort(taskList, (task1, task2) -> task1.getCategory().getName().compareTo(task2.getCategory().getName()));
        taskAdapter.notifyDataSetChanged();
        Log.d("TodoFragment", "Tasks sorted by category.");
    }

    private void sortTasksByName() {
        Collections.sort(taskList, (task1, task2) -> task1.getTask().compareToIgnoreCase(task2.getTask()));
        taskAdapter.notifyDataSetChanged();
        Log.d("TodoFragment", "Tasks sorted by name.");
    }
}
