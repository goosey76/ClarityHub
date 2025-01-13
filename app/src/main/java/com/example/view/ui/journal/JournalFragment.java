package com.example.view.ui.journal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.view.control.bluetooth.ReceiveModeActivity;
import com.example.view.control.journal.JournalViewModel;
import com.example.view.databinding.FragmentJournalBinding;

public class JournalFragment extends Fragment {

    private static final String TAG = "JournalFragment";
    private JournalViewModel journalViewModel;
    private FragmentJournalBinding binding;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    // ActivityResultLauncher to handle result from ReceiveModeActivity
    private final ActivityResultLauncher<Intent> receiveModeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    String receivedMessage = result.getData().getStringExtra("received_message");
                    if (receivedMessage != null) {
                        Log.d(TAG, "Received message: " + receivedMessage);
                    } else {
                        Log.d(TAG, "No message received.");
                    }
                } else {
                    Log.d(TAG, "ReceiveModeActivity canceled or no data received.");
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setupViewModel();

        binding = FragmentJournalBinding.inflate(inflater, container, false);

        initializeBluetooth();

        // Set OnClickListener for the FloatingActionButton
        setupFabClickListener();

        return binding.getRoot();
    }

    private void setupViewModel() {
        journalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);
    }

    private void setupFabClickListener() {
        binding.fabAddJournal.setOnClickListener(v -> {
            // Start receive mode
            startReceiveMode();
        });
    }

    private void initializeBluetooth() {
        bluetoothManager = (BluetoothManager) requireContext().getSystemService(requireContext().BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(getContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startReceiveMode() {
        if (!isBluetoothEnabled()) {
            requestBluetoothEnable();
            return;
        }

        Intent intent = new Intent(getContext(), ReceiveModeActivity.class);
        // Start ReceiveModeActivity and wait for result
        receiveModeLauncher.launch(intent);
    }

    private boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
