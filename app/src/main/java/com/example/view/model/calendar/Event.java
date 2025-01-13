package com.example.view.model.calendar;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Event {
    private String event_id;
    private String title;
    private String category;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int travelTime;
    private String location;
    private String repetition;

    private String notes;
    private List<String> participants;

    public Event(String event_id, String title, String category, LocalDateTime startDateTime,
                 LocalDateTime endDateTime, int travelTime, String location, String repetition,
                 String notes, List<String> participants) {
        this.event_id = event_id;
        this.title = title;
        this.category = category;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.travelTime = travelTime;
        this.location = location;
        this.repetition = repetition;
        this.notes = notes;
        this.participants = participants;
    }

    // Getters and Setters

    public String getId() {
        return event_id;
    }

    public void setId(String event_Id) {
        this.event_id = event_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRepetition() {
        return repetition;
    }

    public void setRepetition(String repetition) {
        this.repetition = repetition;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    /**
     * Gibt eine String-Darstellung des Events zurück, die alle wichtigen Attribute enthält.
     *
     * @return Eine String-Darstellung des Events
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String toString() {

        return "Event{" +
                "eventId='" + event_id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", beginDate='" + startDateTime + '\'' +
                ", endDate='" + endDateTime + '\'' +
                ", travelTime='" + travelTime + '\'' +
                ", location='" + location + '\'' +
                ", repetition='" + repetition + '\'' +
                ", description='" + notes + '\'' +
                ", members=" + participants +
                '}';
    }
}