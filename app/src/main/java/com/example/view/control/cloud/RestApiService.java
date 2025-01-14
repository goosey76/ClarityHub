package com.example.view.control.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.view.control.cloud.ParsingAndSerializer.LocalDateTimeDeserializer;
import com.example.view.control.cloud.ParsingAndSerializer.LocalDateTimeSerializer;
import com.example.view.control.cloud.ParsingAndSerializer.ResponseParser;
import com.example.view.model.calendar.Event;
import com.example.view.model.todo.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;


/**
 * {@code RestApiService} bietet Methoden für die Kommunikation mit einer REST-API.
 * Es umfasst die Generierung einer UUID, das Hinzufügen, Aktualisieren und Löschen
 * von Aufgaben und Kalenderereignissen sowie das Teilen und Abrufen von Ereignissen.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RestApiService {

    /**
     * Basis-URL der API. Diese URL dient als Einstiegspunkt für alle Endpunkte.
     */
    //private static final String BASE_URL = "https://141.144.243.26:25565/api/";
    private static final String BASE_URL = "http://192.168.178.26:8080/api/";

    /**
     * Name der SharedPreferences, in denen die UUID gespeichert wird.
     */
    private static final String PREFS_NAME = "CloudPrefs";
    /**
     * Schlüssel für die UUID in den SharedPreferences.
     */
    private static final String PREF_UUID_KEY = "UUID";


    public static String getUUid (Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_UUID_KEY, null);
    }

    //Hilfsmethode zum Testen der UUID Registrierung im Backend
    // Methode zum Löschen der gespeicherten UUID
    public static void deleteUuid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Prüfen, ob eine UUID existiert
        String existingUuid = sharedPreferences.getString(PREF_UUID_KEY, null);
        if (existingUuid != null) {
            // UUID löschen
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(PREF_UUID_KEY);
            editor.apply();

            Log.d("CloudService", "UUID successfully deleted: " + existingUuid);
        } else {
            Log.d("CloudService", "No UUID found to delete.");
        }
    }

    /**
     * Retrofit-Instanz für die Erstellung von API-Anfragen.
     */
    public static final Retrofit retrofitInstance= new Retrofit.Builder()
            .baseUrl(BASE_URL)
            //.client(createOkHttpClient())
            .addConverterFactory(createGsonConverterFactory()) // JSON-Converter
            .build();


    /**
     * Erstellt und gibt den benutzerdefinierten Gson-Converter zurück, der den LocalDateTimeSerializer beinhaltet.
     */
    private static GsonConverterFactory createGsonConverterFactory() {
            // Erstelle einen GsonBuilder und registriere den LocalDateTimeSerializer
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer()) // Serializer für LocalDateTime
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer()) //Deserializer für LocalDateTime
                    .create();
        // Gebe den GsonConverterFactory mit dem benutzerdefinierten Gson zurück
        return GsonConverterFactory.create(gson);
    }



    /**
     * Definiert die Endpunkte der REST-API.
     */
    public interface ApiService {

    //UUID GENERATOR
        @GET("generate") // Endpunkt: BASE_URL/generate
        Call<ResponseBody> generateUuid(); // GET-Anfrage für UUID

    //TASKS API
        @POST("tasks") // Endpunkt: BASE_URL/tasks
        Call<ResponseBody> sendNewToDo(@Body Task task, @Query("param") String uuid); // POST

        @GET("tasks") // Endpunkt: BASE_URL/tasks
        Call<ResponseBody> getAllToDo(@Query("param") String uuid); //GET-Anfrage für alle ToDos in der Cloud

        @PUT("tasks") // Endpunkt: BASE_URL/tasks
        Call<ResponseBody> sendUpdatedTodo(@Body Task task, @Query("param") String uuid); //PUT-Anfrage, um existierenden ToDos zu aktualisieren

        @DELETE("tasks") // Endpunkt: BASE_URL/tasks
        Call<ResponseBody> deleteToDoInCloud(@Query("idOfDeletedTask") String idOfDeletedTask, @Query("paramUuid") String uuid); //DELETE-Anfrage, um ToDos aus der Cloud zu löschen, basierend auf ihrer ID

    //CALENDAR API
        @POST("calendar") // Endpunkt: BASE_URL/calendar
        Call<ResponseBody> sendNewEvent(@Body Event event, @Query("param") String uuid); // Query-Parameter hinzufügen

        @GET("calendar") // Endpunkt: BASE_URL/calendar
        Call<ResponseBody> getAllEvents(@Query("param") String uuid); //GET-Anfrage für alle Events in der Cloud

        @PUT("calendar") // BASE_URL/calendar
        Call<ResponseBody> sendUpdatedEvent(@Body Event event, @Query("param") String uuid); //PUT-Anfrage, um existierende Events zu aktualisieren

        @DELETE("calendar") // Endpunkt: BASE_URL/calendar
        Call<ResponseBody> deleteEventInCloud(@Query("idOfDeletedEvent") String idOfDeletedEvent, @Query("param") String uuid); //DELETE-Anfrage, um ToDos aus der Cloud zu löschen, basierend auf ihrer ID

    //SHARE API
        @POST("share") // Endpunkt: BASE_URL/share
        Call<ResponseBody> sendEventToShare(@Body Event event); //POST-Anfrage, um Event in öffentliche Datenbank zum teilen zu speichern
            //Implementierung: Check
        @GET("share") // Endpunkt: BASE_URL/share
        Call<ResponseBody> getSharedEvent(@Query("param") String idOfSharedEvent); //GET-Anfrage, um Event aus der öffentlichen Datenbank zu holen, basierend auf der ID des Events

    }

//GENERATE IMPLEMENTIERUNG
    /**
     * Generiert eine UUID, falls diese noch nicht existiert, und speichert sie in den SharedPreferences.
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     */
    public static void generateUuid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String existingUuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        // Prüfen, ob bereits eine UUID existiert
        if (existingUuid != null) {
            Log.d("CloudService", "UUID already exists: " + existingUuid);
            return;
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.generateUuid();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // UUID aus der Antwort extrahieren
                        String uuid = response.body().string();

                        // UUID speichern
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(PREF_UUID_KEY, uuid);
                        editor.apply();

                        Log.d("CloudService", "UUID successfully retrieved and saved: " + uuid);
                    } catch (IOException e) {
                        Log.d("CloudService", "Error parsing UUID: " + e.getMessage());
                    }
                } else {
                    Log.d("CloudService", "Error generating UUID: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("CloudService", "Failed to generate UUID: " + t.getMessage());
            }
        });
    }


//TASKS IMPLEMENTIERUNG
    /**
     * Sendet eine neue Task an die Cloud.
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param taskToStore Die zu speichernde Aufgabe.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static void sendNewToDo(Context context, Task taskToStore) throws MissingUUIDException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.sendNewToDo(taskToStore, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "Task successfully sent");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Failed to send task: " + t.getMessage());
            }
        });
    }

    /**
     * Ruft alle Tasks aus der Cloud ab.
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @return Eine LiveData-Liste mit den Aufgaben aus der Cloud.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static LiveData<List<Task>> getAllToDo(Context context) throws MissingUUIDException {
        MutableLiveData<List<Task>> taskLiveData = new MutableLiveData<>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            taskLiveData.postValue(null);
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.getAllToDo(uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        List<Task> tasks = ResponseParser.parseTaskList(jsonResponse);
                        taskLiveData.postValue(tasks);
                        Log.d("CloudService", "All Tasks successfully retrieved and saved");
                    } catch (Exception e) {
                        Log.e("CloudService", "Error parsing Tasks: " + e.getMessage());
                        taskLiveData.postValue(null);
                    }
                } else {
                    Log.d("CloudService", "Error retrieving all Tasks: " + response.message());
                    taskLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("CloudService", "Failed to retrieve all Tasks: " + t.getMessage());
                taskLiveData.postValue(null);
            }
        });
        return taskLiveData;
    }

    /**
     * Sendet eine aktualisierte Task an die Cloud
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param updatedTask Die zu aktualisierende Task.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static void updateToDoInCloud(Context context, Task updatedTask) throws MissingUUIDException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.sendUpdatedTodo(updatedTask, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "updated Task successfully sent");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error updating Task in Cloud " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Error updating Task in Cloud " + t.getMessage());
            }
        });
    }

    /**
     * Löscht eine Task aus der Cloud
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param idOfDeletedToDo Die zu löschende Task.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static void deleteToDoInCloud(Context context, String idOfDeletedToDo) throws MissingUUIDException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.deleteToDoInCloud(idOfDeletedToDo, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "deleted Task successfully");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error deleting Task in Cloud " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Error deleting Task in Cloud " + t.getMessage());
            }
        });
    }



//CALENDER IMPLEMENTIERUNG

    /**
     * Sendet ein neues Event an die Cloud.
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param eventToStore Das zu speichernde Event
     */
    public static void sendNewEvent(Context context, Event eventToStore) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            return;
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.sendNewEvent(eventToStore, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "Event successfully sent");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error beim POSTen des Events: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Failed to send task: " + t.getMessage());
            }
        });
    }

    /**
     * Rudt alle Events aus der Cloud ab
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @return Eine LiveData-Liste mit den Events aus der Cloud
     */
    public static LiveData<List<Event>> getAllEvents(Context context) {
        MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            eventsLiveData.postValue(null);
            return eventsLiveData;
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.getAllEvents(uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        List<Event> events = ResponseParser.parseEventList(jsonResponse);
                        eventsLiveData.postValue(events);
                        Log.d("CloudService", "All Events successfully retrieved");
                    } catch (Exception e) {
                        Log.e("CloudService", "Error parsing Events: " + e.getMessage());
                        eventsLiveData.postValue(null);
                    }
                } else {
                    Log.d("CloudService", "Error retrieving all Events: " + response.message());
                    eventsLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("CloudService", "Failed to retrieve all Events: " + t.getMessage());
                eventsLiveData.postValue(null);
            }
        });
        return eventsLiveData;
    }

    /**
     * Sendet ein aktualisiertes Event an die Cloud
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param updatedEvent Das zu aktualisierende Event.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static void updateEventInCloud(Context context, Event updatedEvent) throws MissingUUIDException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.sendUpdatedEvent(updatedEvent, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "updated Event successfully sent");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error updating Event in Cloud " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Error updating Event in Cloud " + t.getMessage());
            }
        });
    }

    /**
     * Löscht ein Event aus der Cloud
     *
     * @param context Der Kontext, der für den Zugriff auf SharedPreferences benötigt wird.
     * @param idOfDeletedEvent ID des zu löschenden Events.
     * @throws MissingUUIDException Wenn keine UUID vorhanden ist.
     */
    public static void deleteEventInCloud(Context context, String idOfDeletedEvent) throws MissingUUIDException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString(PREF_UUID_KEY, null);

        if (uuid == null) {
            Log.d("CloudService", "UUID not found. Generate UUID first.");
            throw new MissingUUIDException("keine Bekannte UUID: Kein Cloudaufruf möglich");
        }

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.deleteEventInCloud(idOfDeletedEvent, uuid);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "deleted Event successfully");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error deleting Event in Cloud " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Error deleting Event in Cloud " + t.getMessage());
            }
        });
    }


//SHARE IMPLEMENTIERUNG

    /**
     * Sendet ein Event an den /share Endpunkt, der dort dann vom Empfänger wieder extrahiert werden kann
     *
     * @param eventToShare Das Event, welches geteilt werden soll
     */
    public static void sendEventToShare(Event eventToShare) {

        ApiService apiService = retrofitInstance.create(ApiService.class);
        Log.d("EventToSend", "Event ID: " + eventToShare.getId());
        Call<ResponseBody> call = apiService.sendEventToShare(eventToShare);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Erfolg
                    Log.d("CloudService", "Event successfully sent to " + BASE_URL + "share");
                } else {
                    // Fehler
                    Log.d("CloudService", "Error beim POSTen des Events: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fehler bei der Kommunikation
                Log.d("CloudService", "Failed to send task: " + t.getMessage());
            }
        });
    }

    /**
     * Extrahiert ein Event aus der Cloud basierend auf dessen ID
     *
     * @param idOfSharedEvent ID des Events, welches heruntergeladen werden soll
     * @return Ein LiveData welches die Rückgabe der Cloud zurück gibt
     */
    public static LiveData<Event> getSharedEvent(String idOfSharedEvent) {
        Log.d ("CloudService", "ID of Shared Event: " + idOfSharedEvent);
        //Hilfsklasse zum aynchronen Zurückgeben von CloudRückmeldungen
        MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
        ApiService apiService = retrofitInstance.create(ApiService.class);
        Call<ResponseBody> call = apiService.getSharedEvent(idOfSharedEvent);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Event event = ResponseParser.parseEvent(response.body().string());
                        eventLiveData.postValue(event);
                    } catch (Exception e) {
                        Log.e("CloudService", "Error parsing response: " + e.getMessage());
                    }
                    Log.d("CloudService", "Shared Event successfully retrieved");
                } else {
                    Log.d("CloudService", "Error retrieving SharedEvent: " + response.message());
                    eventLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("CloudService", "Error retrieving SharedEvent: " + t.getMessage());
                eventLiveData.postValue(null);
            }
        });
        return eventLiveData;
    }
}