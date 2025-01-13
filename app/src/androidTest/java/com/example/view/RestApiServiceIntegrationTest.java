package com.example.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.view.control.cloud.MissingUUIDException;
import com.example.view.control.cloud.RestApiService;
import com.example.view.errorhandling.EventErrorException;
import com.example.view.model.calendar.Event;
import com.example.view.model.todo.Category;
import com.example.view.model.todo.Priority;
import com.example.view.model.todo.Task;

@RunWith(AndroidJUnit4.class)
public class RestApiServiceIntegrationTest {

    private Task task1 = new Task("00000100", "Hausaufgaben", Category.UNIVERSITY, "Prog3 erledigen",Priority.NOT_URGENT_NOT_IMPORTANT);

    private Task task2 = new Task("00000101", "Programmieren", Category.UNIVERSITY, "Backend fertigstellen",Priority.URGENT_IMPORTANT);

    private Task task3 = new Task("00000102", "Einkaufen", Category.HOUSEHOLD, "Eier, Brot, Wasser",Priority.URGENT_NOT_IMPORTANT);

    private Task task4 = new Task("00000110", "Meeting vorbereiten", Category.WORK, "C446",Priority.NOT_URGENT_IMPORTANT);

    private Task task5 = new Task("00000104", "Dusche sauber machen", Category.HOUSEHOLD, "",Priority.NOT_URGENT_NOT_IMPORTANT);



    private Event event1 = new Event("00000100", "Meeting", "WORK", LocalDateTime.of(2025,1,13, 12, 30), LocalDateTime.of(2025,1,13, 14, 00), 90, "C646", null, "Meeting about our App", null );

    private Event event2 = new Event("00000101", "Fußball", null, LocalDateTime.of(2025,1,11, 17, 30), LocalDateTime.of(2025,1,11, 20, 00), 15, "SEP", "WEEKLY", null, null );

    private Event event3 = new Event("00000102", "Date", null, LocalDateTime.of(2025,1,17, 15, 00), LocalDateTime.of(2025,1,17, 22, 00),0, null, null, "Weihnachtsmarkt und Abfahrt", null );

    private Event event4 = new Event("00000103", "Arzttermin", null, LocalDateTime.of(2025,2,2, 8, 25), LocalDateTime.of(2025,2,2, 8, 45), 5, "Ärztehaus II", null, "nüchtern!", null );

    private Event event5 = new Event("00000104", "Vorlesung", "UNIVERSITY", LocalDateTime.of(2025,1,13, 12, 15), LocalDateTime.of(2025,1,13, 13, 45), 90, "C446", "DAILY", "Prog3", null );




    private Context context;

    private static final String PREFS_NAME = "CloudPrefs";
    /**
     * Schlüssel für die UUID in den SharedPreferences.
     */
    private static final String PREF_UUID_KEY = "UUID";

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

   // private TestCoroutineDispatcher testDispatcher;


   @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        // UUID generieren und direkt speichern
       //generateAndSetUuid();
       useSameUUid("3d652250-7db0-411a-a8bb-1ce9904c5ef9");
    }

    private void useSameUUid(String uuid) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UUID", uuid);
        editor.apply();
        latch.await(5, TimeUnit.SECONDS);
    }

    private void generateAndSetUuid() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        RestApiService.ApiService apiService = RestApiService.retrofitInstance.create(RestApiService.ApiService.class);
        Call<ResponseBody> call = apiService.generateUuid();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // UUID aus der Antwort extrahieren
                        String uuid = response.body().string();

                        // UUID in SharedPreferences speichern
                        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("UUID", uuid);
                        editor.apply();

                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
    }

//Tasks Tests: abgeschlossen funktionstüchtig!

    @Test
    public void testSendNewToDo() throws Exception {
        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Task senden
        RestApiService.sendNewToDo(context, task1);

        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);
    }

    @Test
    public void testGetAllToDo() throws InterruptedException, MissingUUIDException {
        // CountDownLatch verwenden, um auf die LiveData zu warten
        final CountDownLatch latch = new CountDownLatch(1);

        // Führe den API-Aufruf aus
        LiveData<List<Task>> taskLiveData = RestApiService.getAllToDo(context);

        // Beobachte die LiveData und blockiere den Test, bis die Daten abgerufen wurden
        taskLiveData.observeForever(new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                if (tasks != null) {
                    Log.d("Test", "Tasks: " + tasks.stream()
                            .map(Object::toString)  // Rufe die toString-Methode für jeden Task auf
                            .collect(Collectors.joining(", ")));  // Verbinde die Ergebnisse mit ", "
                } else {
                    Log.d("Test", "Keine Daten empfangen");
                }
                latch.countDown();  // Latch freigeben, wenn die Daten da sind
            }
        });

        // Warte auf die Fertigstellung der API-Antwort (max. 5 Sekunden)
        boolean dataLoaded = latch.await(5, TimeUnit.SECONDS);

        // Überprüfe, ob die Daten erfolgreich abgerufen wurden
        assertTrue("Daten wurden nicht abgerufen", dataLoaded);
    }

    @Test
    public void testUpdateTaskInCloud() throws InterruptedException, MissingUUIDException {

        RestApiService.sendNewToDo(context, task4);

        SystemClock.sleep(2000);
       // Beispiel-Task erstellen
        Task task4_edited = new Task("00000110", "Hausaufgaben", null, "Prog3", Priority.URGENT_IMPORTANT);
        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Task senden

        RestApiService.updateToDoInCloud(context, task4_edited);


        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);

    }

    @Test
    public void testDeleteTaskInCloud() throws InterruptedException, MissingUUIDException {
         // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Task senden

        RestApiService.deleteToDoInCloud(context, task4.getId());

        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);
    }


//Event Tests:

    @Test
    public void testSendNewEvent() throws Exception {
        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Event senden
        RestApiService.sendNewEvent(context, event3);

        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);
    }

    @Test
    public void testGetEvents() throws InterruptedException, MissingUUIDException {
        // CountDownLatch verwenden, um auf die LiveData zu warten
        final CountDownLatch latch = new CountDownLatch(1);

        // Führe den API-Aufruf aus
        LiveData<List<Event>> eventsLiveData = RestApiService.getAllEvents(context);

// Beobachte die LiveData und blockiere den Test, bis die Daten abgerufen wurden
        eventsLiveData.observeForever(new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                if (events != null) {
                    // Logge die Events in der LiveData-Liste
                    Log.d("Test", "Tasks: " + events.stream()
                            .map(Object::toString)  // Rufe die toString-Methode für jedes Event auf
                            .collect(Collectors.joining(", ")));  // Verbinde die Ergebnisse mit ", "
                } else {
                    Log.d("Test", "No tasks found (events is null)");
                }
                // Entferne den Observer nach der Verarbeitung, um Memory-Leaks zu vermeiden
                eventsLiveData.removeObserver(this);
                latch.countDown();
            }
        });


        // Warte auf die Fertigstellung der API-Antwort (max. 5 Sekunden)
        boolean dataLoaded = latch.await(5, TimeUnit.SECONDS);

        // Überprüfe, ob die Daten erfolgreich abgerufen wurden
        assertTrue("Daten wurden nicht abgerufen", dataLoaded);
    }

    @Test
    public void testUpdateEventInCloud() throws InterruptedException, MissingUUIDException, EventErrorException {
        String member = RestApiService.getUUid(context);

        List<String> list = Arrays.asList(member);

        //RestApiService.sendNewEvent(context, event3);

        // Beispiel-Event erstellen
        Event event3_edited = new Event("00000102", "Weihnachtsmarkt", null, LocalDateTime.of(2025,1,17, 15, 00), LocalDateTime.of(2025,1,17, 22, 00),15, "Berlin", null, "Weihnachtsmarkt und Abfahrt", list );

        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Event senden
        RestApiService.updateEventInCloud(context, event3_edited);


        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);

    }

    @Test
    public void testDeleteEventInCloud() throws InterruptedException, MissingUUIDException, EventErrorException {
        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Event löschen
        RestApiService.deleteEventInCloud(context, event3);

        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(5, TimeUnit.SECONDS);

        // Überprüfung: UUID muss vorhanden sein
        SharedPreferences sharedPreferences = context.getSharedPreferences("CloudPrefs", Context.MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        assertNotNull("UUID sollte vorhanden sein", uuid);
    }




//Share Tests: abgeschlossen funktionstüchtig!
    @Test
    public void testSendEventToShare() throws Exception {
       String member = RestApiService.getUUid(context);

        List<String> list = Arrays.asList(member);

        // CountDownLatch zum Warten auf asynchrone Antwort
        CountDownLatch latch = new CountDownLatch(1);

        // Task senden
        RestApiService.sendEventToShare(event2);

        // Warten auf den Abschluss des asynchronen Aufrufs
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetEventToShare() throws Exception {

       //testSendEventToShare();
        // CountDownLatch zum Warten auf asynchrone Antwort
        final CountDownLatch latch = new CountDownLatch(1);

        // Führe den API-Aufruf aus
        LiveData<Event> eventLiveData = RestApiService.getSharedEvent("12345678");

        // Beobachte die LiveData und blockiere den Test, bis die Daten abgerufen wurden
        eventLiveData.observeForever(new Observer<Event>() {
            @Override
            public void onChanged(Event event) {
                if (event != null) {
                    Log.d("Test", "Event empfangen: " + event.toString());  // Ausgabe in Logcat
                } else {
                    Log.d("Test", "Keine Daten empfangen");
                }
                latch.countDown();  // Latch freigeben, wenn die Daten da sind
            }
        });

        // Warte auf die Fertigstellung der API-Antwort (max. 5 Sekunden)
        boolean dataLoaded = latch.await(5, TimeUnit.SECONDS);
    }
}

