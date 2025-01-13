package com.example.view.ui.todo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.view.R;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;

    // Constructor
    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set task title
        holder.textViewTask.setText(task.getTask());

        // Set priority
        holder.textViewPriority.setText("Priorität: " + getPriorityText(task.getPriority()));

        // Set category
        if (task.getCategory() != null) {
            holder.textViewCategory.setText("Kategorie: " + task.getCategory().getName());
            holder.textViewCategory.setVisibility(View.VISIBLE);
        } else {
            holder.textViewCategory.setVisibility(View.GONE);
        }

        // Set description
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.textViewDescription.setText(task.getDescription());
            holder.textViewDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDescription.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0; // Handle null list gracefully
    }

    /**
     * Update the task list and refresh the RecyclerView.
     *
     * @param newTaskList The new list of tasks to display.
     */
    public void updateTaskList(List<Task> newTaskList) {
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    /**
     * Helper method to get human-readable priority text.
     *
     * @param priority The priority enum.
     * @return A human-readable string representation of the priority.
     */
    private String getPriorityText(Priority priority) {
        if (priority == null) return "Unbekannt"; // Handle null priority
        switch (priority) {
            case URGENT_IMPORTANT:
                return "Wichtig & Dringend";
            case NOT_URGENT_IMPORTANT:
                return "Wichtig & Nicht Dringend";
            case URGENT_NOT_IMPORTANT:
                return "Nicht Wichtig & Dringend";
            case NOT_URGENT_NOT_IMPORTANT:
                return "Nicht Wichtig & Nicht Dringend";
            default:
                return "Unbekannt";
        }
    }

    /**
     * ViewHolder class for each task item in the RecyclerView.
     */
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTask, textViewCategory, textViewPriority, textViewDescription;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTask = itemView.findViewById(R.id.textViewTask);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewPriority = itemView.findViewById(R.id.textViewPriority);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
