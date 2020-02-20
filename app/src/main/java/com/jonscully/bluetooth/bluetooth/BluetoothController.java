package com.jonscully.bluetooth.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.Closeable;

public class BluetoothController implements Closeable {
    private static final String TAG = "BluetoothManager";
    private final BluetoothAdapter bluetooth;
    private final BroadcastReceiverDelegator broadcastReceiverDelegator;
    private final Activity context;
    private boolean bluetoothDiscoveryScheduled;
    private BluetoothDevice boundingDevice;

    public BluetoothController(Activity context,BluetoothAdapter adapter, BluetoothDiscoveryDeviceListener listener) {
        this.context = context;
        this.bluetooth = adapter;
        this.broadcastReceiverDelegator = new BroadcastReceiverDelegator(context, listener, this);
    }

    public boolean isBluetoothEnabled() {
        return bluetooth.isEnabled();
    }

    public void startDiscovery() {
        broadcastReceiverDelegator.onDeviceDiscoveryStarted();

        // This line of code is very important. In Android >= 6.0 you have to ask for the runtime
        // permission as well in order for the discovery to get the devices ids. If you don't do
        // this, the discovery won't find any device.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

        // If another discovery is in progress, cancels it before starting the new one.
        if (bluetooth.isDiscovering()) {
            bluetooth.cancelDiscovery();
        }

        // Tries to start the discovery. If the discovery returns false, this means that the
        // bluetooth has not started yet.
        Log.d(TAG, "Bluetooth starting discovery.");
        if (!bluetooth.startDiscovery()) {
            Toast.makeText(context, "Error while starting device discovery!", Toast.LENGTH_SHORT)
                    .show();
            Log.d(TAG, "StartDiscovery returned false. Maybe Bluetooth isn't on?");

            // Ends the discovery.
            broadcastReceiverDelegator.onDeviceDiscoveryEnd();
        }
    }

    public void turnOnBluetooth() {
        Log.d(TAG, "Enabling Bluetooth.");
        broadcastReceiverDelegator.onBluetoothTurningOn();
        bluetooth.enable();
    }

    public boolean pair(BluetoothDevice device) {
        // Stops the discovery and then creates the pairing.
        if (bluetooth.isDiscovering()) {
            Log.d(TAG, "Bluetooth cancelling discovery.");
            bluetooth.cancelDiscovery();
        }
        Log.d(TAG, "Bluetooth bonding with device: " + deviceToString(device));
        boolean outcome = device.createBond();
        Log.d(TAG, "Bounding outcome : " + outcome);

        // If the outcome is true, we are bounding with this device.
        if (outcome == true) {
            this.boundingDevice = device;
        }
        return outcome;
    }

    public boolean isAlreadyPaired(BluetoothDevice device) {
        return bluetooth.getBondedDevices().contains(device);
    }

    public static String deviceToString(BluetoothDevice device) {
        return "[Address: " + device.getAddress() + ", Name: " + device.getName() + "]";
    }

    @Override
    public void close() {
        this.broadcastReceiverDelegator.close();
    }

    public boolean isDiscovering() {
        return bluetooth.isDiscovering();
    }

    public void cancelDiscovery() {
        if(bluetooth != null) {
            bluetooth.cancelDiscovery();
            broadcastReceiverDelegator.onDeviceDiscoveryEnd();
        }
    }

    public void turnOnBluetoothAndScheduleDiscovery() {
        this.bluetoothDiscoveryScheduled = true;
        turnOnBluetooth();
    }

    public void onBluetoothStatusChanged() {
        // Does anything only if a device discovery has been scheduled.
        if (bluetoothDiscoveryScheduled) {

            int bluetoothState = bluetooth.getState();
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_ON:
                    // Bluetooth is ON.
                    Log.d(TAG, "Bluetooth succesfully enabled, starting discovery");
                    startDiscovery();
                    // Resets the flag since this discovery has been performed.
                    bluetoothDiscoveryScheduled = false;
                    break;
                case BluetoothAdapter.STATE_OFF:
                    // Bluetooth is OFF.
                    Log.d(TAG, "Error while turning Bluetooth on.");
                    Toast.makeText(context, "Error while turning Bluetooth on.", Toast.LENGTH_SHORT);
                    // Resets the flag since this discovery has been performed.
                    bluetoothDiscoveryScheduled = false;
                    break;
                default:
                    // Bluetooth is turning ON or OFF. Ignore.
                    break;
            }
        }
    }

    public int getPairingDeviceStatus() {
        if (this.boundingDevice == null) {
            throw new IllegalStateException("No device currently bounding");
        }
        int bondState = this.boundingDevice.getBondState();
        // If the new state is not BOND_BONDING, the pairing is finished, cleans up the state.
        if (bondState != BluetoothDevice.BOND_BONDING) {
            this.boundingDevice = null;
        }
        return bondState;
    }

    public String getPairingDeviceName() {
        return getDeviceName(this.boundingDevice);
    }

    public static String getDeviceName(BluetoothDevice device) {
        String deviceName = device.getName();
        if (deviceName == null) {
            deviceName = device.getAddress();
        }
        return deviceName;
    }

    public boolean isPairingInProgress() {
        return this.boundingDevice != null;
    }

    public BluetoothDevice getBoundingDevice() {
        return boundingDevice;
    }
}
