package com.example.view.control.cloud.ParsingAndSerializer;

import android.os.Build;
import android.util.Log;


import com.example.view.errorhandling.EventErrorException;
import com.example.view.model.calendar.Event;
import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse enthält Methoden zum Parsen von JSON-Antworten und Umwandeln in Java-Objekte.
 * Sie unterstützt das Parsen von Events und Aufgaben sowie deren Listen.
 */
public class ResponseParser {

    /**
     * Parst eine einzelne Event-Antwort von einem JSON-String in ein {@link Event}-Objekt.
     *
     * @param jsonResponse Der JSON-String, der die Event-Daten enthält.
     * @return Ein {@link Event}-Objekt, das die extrahierten Daten enthält.
     * @throws EventErrorException Wenn beim Parsen der Daten ein Fehler auftritt.
     */
    public static Event parseEvent(String jsonResponse) throws EventErrorException {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // JSON-String in ein JSONObject umwandeln
                JSONObject json = new JSONObject(jsonResponse);

                // Felder aus dem JSON extrahieren
                String eventId = json.getString("event_id");
                String title = json.getString("title");

                // Category konvertieren
                String category = json.has("category") && !json.isNull("category")
                        ? json.getString("category").toUpperCase()
                        : null;

                // Datum und Uhrzeit parsen (ISO 8601-Format)
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

                LocalDateTime beginDate = json.has("startDateTime") && !json.isNull("startDateTime")
                        ? LocalDateTime.parse(json.getString("startDateTime"), formatter)
                        : null;

                LocalDateTime endDate = json.has("endDateTime") && !json.isNull("endDateTime")
                        ? LocalDateTime.parse(json.getString("endDateTime"), formatter)
                        : null;

                // Dauer konvertieren
                int travelTime = json.has("travelTime") && !json.isNull("travelTime")
                        ? Integer.parseInt(json.getString("travelTime"))
                        : null;

                // Optionales Feld "location"
                String location = json.has("location") && !json.isNull("location")
                        ? json.getString("location")
                        : null;

                // RepetitionType konvertieren
                String repetition = json.has("repetition") && !json.isNull("repetition")
                        ? json.getString("repetition").toUpperCase()
                        : null;

                // Optionales Feld "description"
                String description = json.has("notes") && !json.isNull("notes")
                        ? json.getString("notes")
                        : null;

                // Mitglieder extrahieren
                List<String> members = new ArrayList<>();
                if (json.has("participants") && !json.isNull("participants")) {
                    JSONArray membersArray = json.getJSONArray("participants");
                    for (int i = 0; i < membersArray.length(); i++) {
                        members.add(membersArray.getString(i));
                    }
                }

                // Event-Objekt erstellen
                return new Event(eventId, title, category, beginDate, endDate, travelTime,
                        location, repetition, description, members);
            } else {
                throw new EventErrorException("Fehler beim Parsen des Events: Build.VERSION.SDK_INT < Build.Version_codes.0");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parst eine Liste von Events aus einem JSON-String.
     *
     * @param jsonResponse Der JSON-String, der die Event-Liste enthält.
     * @return Eine Liste von {@link Event}-Objekten.
     * @throws EventErrorException Wenn beim Parsen der Liste ein Fehler auftritt.
     */
    public static List<Event> parseEventList(String jsonResponse) throws EventErrorException {
        List<Event> events = new ArrayList<>();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // JSON-Array aus der Antwort extrahieren
                JSONArray jsonArray = new JSONArray(jsonResponse);

                // Datum und Uhrzeit parsen (ISO 8601-Format)
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);

                    // Felder aus dem JSON extrahieren
                    String eventId = json.getString("event_id");
                    String title = json.getString("title");

                    // Category konvertieren
                    String category = json.has("category") && !json.isNull("category")
                            ? json.getString("category").toUpperCase()
                            : null;

                    LocalDateTime beginDate = json.has("startDateTime") && !json.isNull("startDateTime")
                            ? LocalDateTime.parse(json.getString("startDateTime"), formatter)
                            : null;

                    LocalDateTime endDate = json.has("endDateTime") && !json.isNull("endDateTime")
                            ? LocalDateTime.parse(json.getString("endDateTime"), formatter)
                            : null;

                    // Dauer konvertieren
                    int travelTime = json.has("travelTime") && !json.isNull("travelTime")
                            ? Integer.parseInt(json.getString("travelTime"))
                            : null;

                    // Optionales Feld "location"
                    String location = json.has("location") && !json.isNull("location")
                            ? json.getString("location")
                            : null;

                    // RepetitionType konvertieren
                    String repetition = json.has("repetition") && !json.isNull("repetition")
                            ? json.getString("repetition").toUpperCase()
                            : null;

                    // Optionales Feld "description"
                    String description = json.has("notes") && !json.isNull("notes")
                            ? json.getString("notes")
                            : null;

                    // Mitglieder extrahieren
                    List<String> members = new ArrayList<>();
                    if (json.has("participants") && !json.isNull("participants")) {
                        JSONArray membersArray = json.getJSONArray("participants");
                        for (int a = 0; a < membersArray.length(); a++) {
                            members.add(membersArray.getString(a));
                        }
                    }

                    // Event-Objekt erstellen und zur Liste hinzufügen
                    Event event = new Event(eventId, title, category, beginDate, endDate,
                            travelTime, location, repetition, description, members);
                    events.add(event);
                }
            }else {
                    throw new EventErrorException("Fehler beim Parsen des Events: Build.VERSION.SDK_INT < Build.Version_codes.0");
                }
        } catch (Exception e) {
            // Fehlerbehandlung
            throw new EventErrorException("Fehler beim Parsen der Event-Liste: " + e.getMessage());
        }
        return events;
    }

    /**
     * Parst eine Liste von Tasks aus einem JSON-String.
     *
     * @param jsonResponse Der JSON-String, der die Task-Liste enthält.
     * @return Eine Liste von {@link Task}-Objekten.
     * @throws Exception Wenn beim Parsen der Liste ein Fehler auftritt.
     */
    public static List<Task> parseTaskList(String jsonResponse) throws Exception {
        List<Task> tasks = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                // Extrahiere die Felder aus dem JSON-Objekt
                String id = json.getString("id");
                String taskName = json.getString("task");
                Category category = json.has("category") && !json.isNull("category")
                        ? Category.valueOf(json.getString("category").toUpperCase())
                        : null;
                String description = json.has("description") && !json.isNull("description")
                        ? json.getString("description")
                        : null;
                Priority priority = json.has("priority") && !json.isNull("priority")
                        ? Priority.valueOf(json.getString("priority"))
                        : null;

                // Erstelle ein Task-Objekt und füge es zur Liste hinzu
                Task task = new Task(id, taskName, category, description, priority);
                tasks.add(task);
            }
        } catch (Exception e) {
            throw new Exception("Error parsing Task list: " + e.getMessage());
        }
        return tasks;
    }
}
