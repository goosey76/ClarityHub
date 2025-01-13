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
import com.example.view.control.calendar.CalendarViewModelFactory;
import com.example.view.model.calendar.Event;
import com.example.view.model.calendar.EventDialogHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    // UI Components
    private CalendarView calendarView;
    private LinearLayout eventsContainer;
    private FloatingActionButton fabAddEvent;

    // Data
    private CalendarViewModel calendarViewModel;
    private LocalDateTime selectedDate = LocalDateTime.now();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize views
        calendarView = view.findViewById(R.id.calendarView);
        eventsContainer = view.findViewById(R.id.eventsContainer);
        fabAddEvent = view.findViewById(R.id.fabAddEvent);

        // Set up ViewModel
        CalendarViewModelFactory factory = new CalendarViewModelFactory(requireContext());
        calendarViewModel = new ViewModelProvider(this, factory).get(CalendarViewModel.class);

        // Set up listeners
        setupCalendarView();
        setupFab();

        // Observe LiveData
        observeEvents();

        return view;
    }

    private void observeEvents() {
        calendarViewModel.getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            if (eventsContainer != null) {
                updateEventsUI(events);
            } else {
                Log.e("CalendarFragment", "eventsContainer is null. Cannot update UI.");
            }
        });
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
            calendarViewModel.loadEventsForDate(selectedDate);
        });
    }

    private void setupFab() {
        fabAddEvent.setOnClickListener(v -> showAddEventDialog());
    }

    private void showAddEventDialog() {
        EventDialogHelper dialogHelper = new EventDialogHelper(requireContext(), calendarViewModel, null);

        dialogHelper.setOnDismissListener(() -> calendarViewModel.loadEventsForDate(selectedDate));
        dialogHelper.show();
    }

    private void showEditEventDialog(Event event) {
        if (event == null) {
            Log.e("CalendarFragment", "Event is null. Cannot edit.");
            return;
        }

        // Pass the event to the dialog
        EventDialogHelper dialogHelper = new EventDialogHelper(requireContext(), calendarViewModel, event);

        dialogHelper.setOnDismissListener(() -> {
            // Refresh events for the selected date after editing
            calendarViewModel.loadEventsForDate(selectedDate);
        });

        dialogHelper.show();
    }


    private void deleteEvent(Event event) {
        if (event != null) {
            calendarViewModel.deleteEvent(event.getId());
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();

            // Refresh the events list for the current date
            calendarViewModel.loadEventsForDate(selectedDate);
        }
    }

    private void updateEventsUI(List<Event> events) {
        eventsContainer.removeAllViews(); // Clear the container before adding new events

        if (events != null && !events.isEmpty()) {
            for (Event event : events) {
                // Create a new TextView or styled view for each event
                TextView eventView = new TextView(requireContext());
                eventView.setText(formatEventDetails(event));
                eventView.setTextSize(16);
                eventView.setPadding(16, 16, 16, 16);
                eventView.setBackgroundResource(R.drawable.calming_background); // Replace with an actual background drawable
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
    }

    private String formatEventDetails(Event event) {
        if (event == null) return "";
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

        return event.getTitle() + " (" +
                event.getStartDateTime().format(timeFormatter) + " - " +
                event.getEndDateTime().format(timeFormatter) + ")";
    }
}
