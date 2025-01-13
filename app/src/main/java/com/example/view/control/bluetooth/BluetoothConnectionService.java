package com.example.view.control.bluetooth;
// BluetoothConnectionService.java


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID
    private static BluetoothConnectionService instance;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected = false;

    private BluetoothConnectionService() {}

    public static BluetoothConnectionService getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionService();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device, String message) {
        new Thread(() -> {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();

                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

                isConnected = true;

                // Send the message
                write(message.getBytes());

                // Start listening for incoming messages
                startListening();

            } catch (IOException e) {
                Log.e(TAG, "Error connecting to device", e);
                close();
            }
        }).start();
    }

    private void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (isConnected) {
                try {
                    bytes = inputStream.read(buffer);
                    String message = new String(buffer, 0, bytes);

                    // Handle received message on main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Handle the received message here
                        Log.d(TAG, "Received message: " + message);
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from input stream", e);
                    break;
                }
            }
        }).start();
    }

    public void write(byte[] data) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to output stream", e);
        }
    }

    public void close() {
        isConnected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }
    }
}