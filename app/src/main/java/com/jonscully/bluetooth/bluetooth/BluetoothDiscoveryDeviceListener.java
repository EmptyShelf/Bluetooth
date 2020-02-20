package com.jonscully.bluetooth.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDiscoveryDeviceListener {
    void onDeviceDiscovered(BluetoothDevice device);
    void onDeviceDiscoveryStarted();
    void setBluetoothController(BluetoothController bluetooth);
    void onDeviceDiscoveryEnd();
    void onBluetoothStatusChanged();
    void onBluetoothTurningOn();
    void onDevicePairingEnded();
}
