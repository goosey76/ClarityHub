package com.example.view.control.bluetooth;
// DeviceListActivity.java


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.view.R;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ListView deviceList;
    private ArrayList<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        String idOfEventToShare = getIntent().getSerializableExtra("idOfEventToShare").toString();

        deviceList = findViewById(R.id.deviceList);
        devices = new ArrayList<>();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        showPairedDevices();

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = devices.get(position);
            BluetoothConnectionService.getInstance().connectToDevice(device, idOfEventToShare);
            finish();
        });
    }

    private void showPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> deviceNames = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(device);
                deviceNames.add(device.getName() + "\n" + device.getAddress());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, deviceNames);
        deviceList.setAdapter(adapter);
    }
}
