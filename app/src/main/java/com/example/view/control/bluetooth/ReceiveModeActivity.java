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

import com.example.view.R;

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
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(this, "Message received: " + receivedMessage, Toast.LENGTH_LONG).show();

            // Zurück zur Host-Activity und Nachricht als Extra übergeben
            Intent intent = new Intent();
            intent.putExtra("received_message", receivedMessage);
            setResult(RESULT_OK, intent); // Setzt das Ergebnis für die aufrufende Activity
            finish(); // Schließt die ReceiveModeActivity
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
