package com.example.view.control.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.view.R;
import com.example.view.control.cloud.RestApiService;
import com.example.view.model.calendar.Event;
import com.example.view.model.repository.EventRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ReceiveModeActivity extends AppCompatActivity {
    private static final String TAG = "ReceiveModeActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activvity_receive_mode);

        statusText = findViewById(R.id.statusText);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startListening();
    }

    private void startListening() {
        acceptThread = new AcceptThread();
        acceptThread.start();
        updateStatus("Listening for connections...");
    }

    private void updateStatus(String message) {
        new Handler(Looper.getMainLooper()).post(() -> statusText.setText(message));
    }

    private void returnToHostActivity(String receivedMessage) {


        // Since this is called from AcceptThread (background thread),
        // we need to switch to the main thread to observe LiveData
        new Handler(Looper.getMainLooper()).post(() -> {

            // Show loading state
            updateStatus("Processing received data...");

            // Get LiveData from API
            LiveData<Event> receivedEventLiveData = RestApiService.getSharedEvent(receivedMessage);

            // Now we're on the main thread, we can safely observe LiveData
            receivedEventLiveData.observe(this, event -> {
                // Remove the observer after first result
                receivedEventLiveData.removeObservers(this);

                if (event != null) {
                    // Move database operation to background thread
                    new Thread(() -> {
                        try {
                            EventRepository repository = new EventRepository(this);
                            repository.insertEvent(event);

                            // Switch back to main thread for UI updates
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(this,
                                        "Event saved: " + event.getTitle(),
                                        Toast.LENGTH_LONG).show();

                                Toast.makeText(this, receivedMessage, Toast.LENGTH_LONG).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("received_message", receivedMessage);
                                resultIntent.putExtra("event_id", event.getId());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving event", e);
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(this,
                                        "Error saving event: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                setResult(RESULT_CANCELED);
                                finish();
                            });
                        }
                    }).start();
                } else {
                    Toast.makeText(this,
                            "Error retrieving event data",
                            Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        });
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothApp", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    manageConnectedSocket(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket socket) {
            updateStatus("Connected!");

            byte[] buffer = new byte[1024];
            int bytes;

            try {
                InputStream inputStream = socket.getInputStream();
                // Read only once - first message received
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    String message = new String(buffer, 0, bytes);
                    returnToHostActivity(message);
                }
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error reading from socket", e);
                updateStatus("Connection lost");
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptThread != null) {
            acceptThread.cancel();
        }
    }
}
