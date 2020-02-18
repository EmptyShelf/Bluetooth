package com.jonscully.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
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
        CheckBox cbTurnOnBluetooth = findViewById(R.id.cbTurnOnBluetooth);
        CheckBox cbMakeDiscoverable = findViewById(R.id.cbMakeDiscoverable);
        CheckBox cbShowPairedDevices = findViewById(R.id.cbShowPairedDevices);


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
            cbTurnOnBluetooth.setChecked(true);
        }
        else {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
            cbTurnOnBluetooth.setChecked(false);
        }

        cbTurnOnBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mBluetoothAdapter.isEnabled()){
                        showToast("Bluetooth is on");
                    }
                    else {
                        showToast("Turning Bluetooth on...");
                        //intent to on bluetooth
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                    }
                } else {
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

        // Turn On button
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()){
                    showToast("Turning Bluetooth on...");
                    //intent to on bluetooth
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // show message
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
