package com.example.view.model.todo;

public enum Category {

    WORK("Arbeit", "Office", 30, -1),
    UNIVERSITY("Uni", "HTW Berlin", 90, 90),
    HOUSEHOLD("Haushalt", "Home", -1, -1);

    private String name;
    private String location;
    private int travelTime; // in Minuten, -1 falls nicht angegeben
    private int duration;  // in Minuten, -1 falls nicht angegeben

    // Konstruktor
    Category(String name, String location, int travelTime, int duration) {
        this.name = name;
        this.location = location;
        this.travelTime = travelTime;
        this.duration = duration;
    }

    public static Category fromName(String name) {
        for (Category category : values()) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No enum constant for name: " + name);
    }

    // Getter
    public String getName() {
        return name;
    }


    public String getLocation() {
        return location;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getDuration() {
        return duration;
    }

    // Setter
    public void setLocation(String location) {
        this.location = location;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // Details der Kategorie als String
    public String getDetails() {
        return String.format(
                "Name: %s, Location: %s, Travel Time: %s, Duration: %s",
                name,
                location,
                (travelTime == -1 ? "Not specified" : travelTime + " minutes"),
                (duration == -1 ? "Not specified" : duration + " minutes")
        );
    }
}
