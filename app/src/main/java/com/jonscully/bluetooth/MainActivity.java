package com.jonscully.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    private TextView mPairedTv;
    private ImageView mBlueIv;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv     = findViewById(R.id.pairedTv);
        mBlueIv       = findViewById(R.id.bluetoothIv);
        Button mOnBtn = findViewById(R.id.onBtn);
        Button mOffBtn = findViewById(R.id.offBtn);
        Button mDiscoverBtn = findViewById(R.id.discoverableBtn);
        Button mPairedBtn = findViewById(R.id.pairedBtn);
        CheckBox cbEnableBluetooth = findViewById(R.id.cbEnableBluetooth);
        CheckBox cbDiscoverableDevice = findViewById(R.id.cbDiscoverableDevice);
        CheckBox cbListPairedDevices = findViewById(R.id.cbListPairedDevices);

        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();

        // check Bluetooth availability
        if (mBluetoothAdapter == null){
            mStatusBlueTv.setText(R.string.stBluetoothIsNotAvailable);
        }
        else {
            mStatusBlueTv.setText(R.string.stBluetoothIsAvailable);
        }

        // set Bluetooth status icon
        if (mBluetoothAdapter.isEnabled()){
            mBlueIv.setImageResource(R.drawable.ic_action_on);
            cbEnableBluetooth.setChecked(true);
        }
        else {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
            cbEnableBluetooth.setChecked(false);
        }

        cbEnableBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBluetoothAdapter.isEnabled()){
                        showToast("Bluetooth is on");
                    }
                    else {
                        showToast("Turning Bluetooth on...");
                        final Intent intentDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intentDiscoverable, REQUEST_ENABLE_BT);
                    }
                }
                else {
                    if (mBluetoothAdapter.isEnabled()){
                        mBluetoothAdapter.disable();
                        showToast("Turning Bluetooth off");
                        mBlueIv.setImageResource(R.drawable.ic_action_off);
                    }
                    else {
                        showToast("Bluetooth is off");
                    }
                }
            }
        });


        cbDiscoverableDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        showToast("Device discovery is on");
                    } else {
                        showToast("Making this device discoverable");
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        //intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivityForResult(intent, REQUEST_DISCOVER_BT);

                    }
                }
                else {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                        showToast("Canceling device discovery");
                    }
                    else {
                        showToast("Device discovery is off");
                    }
                }
            }
        });

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


        cbListPairedDevices.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBluetoothAdapter.isEnabled()){
                        mPairedTv.setText(R.string.stPairedDevices);
                        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device: devices){
                            mPairedTv.append("\nDevice: " + device.getName()+ ", " + device);
                        }
                    }
                    else {
                        showToast("Bluetooth is off");
                    }
                }
                else {
                    mPairedTv.setText("");
                }
            }
        });

        // Turn On button
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()){
                    showToast("Turning Bluetooth on...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else {
                    showToast("Bluetooth is on");
                }
            }
        });

        // Turn Off button
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    showToast("Turning Bluetooth off");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
                else {
                    showToast("Bluetooth is off");
                }
            }
        });

        // Discoverable button
        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isDiscovering()){
                    showToast("Making this device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        // Get Paired Devices button
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()){
                    mPairedTv.setText(R.string.stPairedDevices);
                    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device: devices){
                        mPairedTv.append("\nDevice: " + device.getName()+ ", " + device);
                    }
                }
                else {
                    // Bluetooth is off
                    showToast("Bluetooth is off");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else {
                    showToast("Bluetooth is off (no access)");
                }
                break;
            case REQUEST_DISCOVER_BT:
                if (resultCode == RESULT_OK){
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else {
                    showToast("Bluetooth is off (no access)");
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // show message
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
