package com.example.view.ui.calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.view.R;
import com.example.view.control.calendar.CalendarViewModel;
import com.example.view.model.calendar.Event;
import com.example.view.model.calendar.EventDialogHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    // UI Components
    private CalendarView calendarView;
    private LinearLayout eventsContainer;
    private FloatingActionButton fabAddEvent;

    // Data
    private CalendarViewModel calendarViewModel; // Use CalendarViewModel
    private LocalDateTime selectedDate = LocalDateTime.now();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initializeViews(view);
        setupViewModel();
        setupListeners();
        observeEvents();
        return view;
    }

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        eventsContainer = view.findViewById(R.id.eventsContainer);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);
    }

    private void setupViewModel() {
        // Initialize CalendarViewModel
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
    }

    private void setupListeners() {
        setupCalendarView();
        setupFab();
    }

    private void showAddEventDialog() {
        EventDialogHelper dialogHelper = new EventDialogHelper(requireContext(), calendarViewModel, null);

        dialogHelper.setOnDismissListener(() -> {
            calendarViewModel.loadEventsForDate(selectedDate);
        });

        dialogHelper.show();
    }

    private void showEditEventDialog(Event event) {
        if (event == null || event.getStartDateTime() == null) {
            Log.e("CalendarFragment", "Event or its startDateTime is null");
            Toast.makeText(getContext(), "Invalid event data", Toast.LENGTH_SHORT).show();
            return;
        }

        EventDialogHelper dialogHelper = new EventDialogHelper(requireContext(), calendarViewModel, event);

        dialogHelper.setOnDismissListener(() -> {
            calendarViewModel.loadEventsForDate(selectedDate);
        });

        dialogHelper.show();
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
            calendarViewModel.loadEventsForDate(selectedDate); // Load events for the selected date
        });
    }

    private void setupFab() {
        fabAddEvent.setOnClickListener(v -> showAddEventDialog());
    }

    private void deleteEvent(Event event) {
        if (event != null) {
            calendarViewModel.deleteEvent(event.getId()); // Call the ViewModel method to delete the event
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();

            // Refresh the events list for the current date
            calendarViewModel.loadEventsForDate(selectedDate);
        }
    }

    private void observeEvents() {
        calendarViewModel.getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            eventsContainer.removeAllViews(); // Clear the container before adding new events

            if (events != null && !events.isEmpty()) {
                for (Event event : events) {
                    // Create a new TextView or CardView for each event
                    TextView eventView = new TextView(requireContext());
                    eventView.setText(formatEventDetails(event));
                    eventView.setTextSize(16);
                    eventView.setPadding(16, 16, 16, 16);
                    eventView.setBackgroundResource(R.drawable.ic_launcher_background); // Optional styling
                    eventView.setTag(event); // Attach the event object to the view

                    // Set up click listener for editing
                    eventView.setOnClickListener(v -> showEditEventDialog((Event) v.getTag()));

                    // Set up long click listener for deleting
                    eventView.setOnLongClickListener(v -> {
                        deleteEvent((Event) v.getTag());
                        return true;
                    });

                    // Add the eventView to the container
                    eventsContainer.addView(eventView);
                }
            } else {
                // If no events, display a placeholder message
                TextView noEventsView = new TextView(requireContext());
                noEventsView.setText("No events for this date");
                noEventsView.setTextSize(16);
                noEventsView.setPadding(16, 16, 16, 16);
                eventsContainer.addView(noEventsView);
            }
        });
    }

    private String formatEventDetails(Event event) {
        if (event == null) return "";
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

        return event.getTitle() + " (" +
                event.getStartDateTime().format(timeFormatter) + " - " +
                event.getEndDateTime().format(timeFormatter) + ")";
    }
}
