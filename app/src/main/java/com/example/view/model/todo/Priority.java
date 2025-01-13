package com.example.view.model.todo;

public enum Priority {
    URGENT_IMPORTANT(1),
    NOT_URGENT_IMPORTANT(2),
    URGENT_NOT_IMPORTANT(3),
    NOT_URGENT_NOT_IMPORTANT(4);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Priority fromValue(int value) {
        for (Priority priority : Priority.values()) {
            if (priority.getValue() == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

}
