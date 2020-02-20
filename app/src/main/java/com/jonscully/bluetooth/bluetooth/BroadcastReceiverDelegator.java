package com.jonscully.bluetooth.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.Closeable;

public class BroadcastReceiverDelegator extends BroadcastReceiver implements Closeable {
    private final BluetoothDiscoveryDeviceListener listener;
    private final String TAG = "BroadcastReceiver";
    private final Context context;

    public BroadcastReceiverDelegator(Context context, BluetoothDiscoveryDeviceListener listener, BluetoothController bluetooth) {
        this.listener = listener;
        this.context = context;
        this.listener.setBluetoothController(bluetooth);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Incoming intent : " + action);
        switch (action) {
            case BluetoothDevice.ACTION_FOUND :
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device discovered! " + BluetoothController.deviceToString(device));
                listener.onDeviceDiscovered(device);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED :
                // Discovery has ended.
                Log.d(TAG, "Discovery ended.");
                listener.onDeviceDiscoveryEnd();
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED :
                // Discovery state changed.
                Log.d(TAG, "Bluetooth state changed.");
                listener.onBluetoothStatusChanged();
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED :
                // Pairing state has changed.
                Log.d(TAG, "Bluetooth bonding state changed.");
                listener.onDevicePairingEnded();
                break;
            default :
                // Does nothing.
                break;
        }
    }

    public void onDeviceDiscoveryStarted() {
        listener.onDeviceDiscoveryStarted();
    }

    public void onDeviceDiscoveryEnd() {
        listener.onDeviceDiscoveryEnd();
    }

    public void onBluetoothTurningOn() {
        listener.onBluetoothTurningOn();
    }

    @Override
    public void close() {
        context.unregisterReceiver(this);
    }
}
