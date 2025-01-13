package com.example.view.model.calendar;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import com.example.view.control.bluetooth.DeviceListActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.view.R;
import com.example.view.control.calendar.CalendarViewModel;
import com.example.view.control.cloud.RestApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EventDialogHelper {

    private BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final Dialog dialog;
    private final View dialogView;
    private final CalendarViewModel viewModel;

    // UI Components
    private TextInputEditText titleInput, notesInput, participantsInput;
    private AutoCompleteTextView categoryDropdown, locationInput, repetitionDropdown;
    private MaterialButton startDateTimeButton, endDateTimeButton, saveButton, cancelButton, deleteButton, sendenButton;
    private Slider travelTimeSlider;

    private LocalDateTime selectedStartDateTime;
    private LocalDateTime selectedEndDateTime;

    private Runnable onDismissListener;

    public EventDialogHelper(Context context, CalendarViewModel viewModel, @Nullable Event eventToEdit) {
        this.context = context;
        this.viewModel = viewModel;

        dialog = new Dialog(context);
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null);
        dialog.setContentView(dialogView);

        initializeViews();

        if (eventToEdit != null) {
            selectedStartDateTime = eventToEdit.getStartDateTime();
            selectedEndDateTime = eventToEdit.getEndDateTime();
            prefillEventData(eventToEdit);
        } else {
            selectedStartDateTime = LocalDateTime.now();
            selectedEndDateTime = selectedStartDateTime.plusHours(1); // Default end time
        }

        setupListeners(eventToEdit);
        setupDropdowns();
    }

    public void setOnDismissListener(Runnable onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void show() {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setOnDismissListener(d -> {
            if (onDismissListener != null) {
                onDismissListener.run();
            }
        });
        dialog.show();
    }

    private void initializeViews() {
        titleInput = dialogView.findViewById(R.id.titleInput);
        notesInput = dialogView.findViewById(R.id.notesInput);
        participantsInput = dialogView.findViewById(R.id.participantsInput);
        categoryDropdown = dialogView.findViewById(R.id.categoryDropdown);
        locationInput = dialogView.findViewById(R.id.locationInput);
        repetitionDropdown = dialogView.findViewById(R.id.repetitionDropdown);
        startDateTimeButton = dialogView.findViewById(R.id.startDateTimeButton);
        endDateTimeButton = dialogView.findViewById(R.id.endDateTimeButton);
        saveButton = dialogView.findViewById(R.id.saveButton);
        cancelButton = dialogView.findViewById(R.id.cancelButton);
        deleteButton = dialogView.findViewById(R.id.deleteButton);
        travelTimeSlider = dialogView.findViewById(R.id.travelTimeSlider);
        sendenButton = dialogView.findViewById(R.id.sendButton);
    }

    private void prefillEventData(Event event) {
        titleInput.setText(event.getTitle());
        categoryDropdown.setText(event.getCategory(), false);
        startDateTimeButton.setText(formatDate(event.getStartDateTime()));
        endDateTimeButton.setText(formatDate(event.getEndDateTime()));
        travelTimeSlider.setValue(event.getTravelTime());
        locationInput.setText(event.getLocation());
        repetitionDropdown.setText(event.getRepetition(), false);
        notesInput.setText(event.getNotes());
        participantsInput.setText(String.join(", ", event.getParticipants()));
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void setupListeners(@Nullable Event eventToEdit) {
        startDateTimeButton.setOnClickListener(v -> showDateTimePicker(true));
        endDateTimeButton.setOnClickListener(v -> showDateTimePicker(false));

        sendenButton.setOnClickListener(v ->
                {
                Event newEvent = createEvent();
                RestApiService.sendEventToShare(newEvent);
                showPairedDevices(newEvent.getId());
            }
        );

        saveButton.setOnClickListener(v -> {
            if (eventToEdit == null) saveEvent();
            else updateEvent(eventToEdit);
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        if (eventToEdit != null) {
            deleteButton.setOnClickListener(v -> {
                viewModel.deleteEvent(eventToEdit.getId());
                dialog.dismiss();
            });
        }
    }

    private void showPairedDevices(String idOfEventToShare) {
        Intent intent1 = new Intent(context, DeviceListActivity.class);
        intent1.putExtra("idOfEventToShare", idOfEventToShare);
        if (context instanceof Activity) {
            context.startActivity(intent1);
        } else {
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }



    private void setupDropdowns() {
        List<String> categories = Arrays.asList("Uni", "Sport", "Arbeit", "Haushalt");
        categoryDropdown.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categories));

        List<String> repetitions = Arrays.asList("Keine", "Täglich", "Wöchentlich", "Monatlich");
        repetitionDropdown.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, repetitions));
    }

    private void saveEvent() {
        Event newEvent = createEvent();
        viewModel.addEvent(newEvent);
        dialog.dismiss();
    }

    private Event createEvent () {
        Event newEvent = new Event(
                UUID.randomUUID().toString(),
                titleInput.getText().toString(),
                categoryDropdown.getText().toString(),
                selectedStartDateTime,
                selectedEndDateTime,
                (int) travelTimeSlider.getValue(),
                locationInput.getText().toString(),
                repetitionDropdown.getText().toString(),
                notesInput.getText().toString(),
                Arrays.asList(participantsInput.getText().toString().split(",")));
        return newEvent;
    }


    private void updateEvent(Event eventToEdit) {
        eventToEdit.setTitle(titleInput.getText().toString());
        eventToEdit.setCategory(categoryDropdown.getText().toString());
        eventToEdit.setStartDateTime(selectedStartDateTime);
        eventToEdit.setEndDateTime(selectedEndDateTime);
        eventToEdit.setTravelTime((int) travelTimeSlider.getValue());
        eventToEdit.setLocation(locationInput.getText().toString());
        eventToEdit.setRepetition(repetitionDropdown.getText().toString());
        eventToEdit.setNotes(notesInput.getText().toString());
        eventToEdit.setParticipants(Arrays.asList(participantsInput.getText().toString().split(",")));

        viewModel.updateEvent(eventToEdit);
        dialog.dismiss();
    }

    private void showDateTimePicker(boolean isStart) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection((isStart ? selectedStartDateTime : selectedEndDateTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDateTime selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDateTime();
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setHour(isStart ? selectedStartDateTime.getHour() : selectedEndDateTime.getHour())
                    .setMinute(isStart ? selectedStartDateTime.getMinute() : selectedEndDateTime.getMinute())
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                LocalDateTime finalDateTime = selectedDate.withHour(timePicker.getHour()).withMinute(timePicker.getMinute());
                if (isStart) {
                    selectedStartDateTime = finalDateTime;
                    startDateTimeButton.setText(formatDate(finalDateTime));
                } else {
                    selectedEndDateTime = finalDateTime;
                    endDateTimeButton.setText(formatDate(finalDateTime));
                }
            });

            timePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "timePicker");
        });

        datePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "datePicker");
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault()));
    }
}
