package com.example.view.model.todo;

public class Task {

    private String id;
    private String task;
    private Category category;
    private String description;
    private Priority priority;

    public Task(String id, String task, Category category, String description, Priority priority) {
        this.id = id;
        this.task = task;
        this.category = category;
        this.description = description;
        this.priority = priority; //
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
    
    public Priority getPriority() {
        return priority;
    }

    // Helper Method to get category details
    public String getCategoryDetails() {
        return category != null ? category.getDetails() : "No category specified";
    }

    // Helper Method to get priority details
    public int getPriorityDetails() {
        return priority != null ? priority.getValue() : -1;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", task='" + task + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                '}';
    }


    public String getTitle() {
        return task;
    }
}
