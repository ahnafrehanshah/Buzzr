package com.example.buzzr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.DeviceListAdapter;
import com.example.buzzr.HelperClasses.userHelperClass;

import java.util.ArrayList;

public class BluetoothScreen extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button bluetoothSwitchBtn, discoverBtn;
    ListView deviceList;

    boolean registerBR1 = false, registerBR2 = false, registerBR3 = false;

    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> BTDevices = new ArrayList<>();
    DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_screen);

        //Hooks
        bluetoothSwitchBtn = findViewById(R.id.bluetoothSwitch);
        discoverBtn = findViewById(R.id.bluetoothDiscover);
        deviceList = findViewById(R.id.deviceList);

        //Animations

        //Bluetooth
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiverPair, filter);
        registerBR3 = true;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList.setOnItemClickListener(BluetoothScreen.this);

        if (bluetoothAdapter.isEnabled()) {
            bluetoothSwitchBtn.setText("Turn off Bluetooth");
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothSwitchBtn.setText("Turn on Bluetooth");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void discoverDevices(View view) {
        deviceList.setAdapter(null);
        BTDevices.clear();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();

            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiverDiscovery, discoverDeviceIntent);
            registerBR2 = true;
        }

        if (!bluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiverDiscovery, discoverDeviceIntent);
            registerBR2 = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }

    public void switchBluetooth(View view) {
        if (bluetoothAdapter == null) {
            Log.d("BluetoothScreen", "Device doesn't have bluetooth");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver, BTIntent);
            registerBR1 = true;
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            deviceList.setAdapter(null);
            BTDevices.clear();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver, BTIntent);
            registerBR1 = true;
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(bluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:{
                        bluetoothSwitchBtn.setText("Turn On Bluetooth");
                        Log.d("BluetoothScreen", "onReceive: STATE OFF");
                        break;
                    }
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("BluetoothScreen", "broadcastReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothSwitchBtn.setText("Turn off Bluetooth");
                        Log.d("BluetoothScreen", "broadcastReceiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BluetoothScreen", "broadcastReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    if (!BTDevices.contains(device)) {
                        BTDevices.add(device);
                    }
                }

                Log.d("Bluetooth String", "onReceive: " + device.getName() + ": " + device.getAddress());

                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_list_layout, BTDevices);
                deviceList.setAdapter((ListAdapter) deviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverPair = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d("BluetoothScreen", "BroadcastReceiver: BOND_BONDED");

                    //SharedPreferences
                    userHelperClass userData = new userHelperClass(getApplicationContext());
                    userData.setDeviceName(device.getName());

                    finish();
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d("BluetoothScreen", "BroadcastReceiver: BOND_BONDING");
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("BluetoothScreen", "BroadcastReceiver: BOND_NONE");
                    userHelperClass userData = new userHelperClass(getApplicationContext());
                    userData.setDeviceName("No devices found");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (registerBR1) {
            unregisterReceiver(broadcastReceiver);
        }
        if (registerBR2) {
            unregisterReceiver(broadcastReceiverDiscovery);
        }
        if (registerBR3) {
            unregisterReceiver(broadcastReceiverPair);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();

        String deviceName = BTDevices.get(position).getName();
        String deviceAddress = BTDevices.get(position).getAddress();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BTDevices.get(position).createBond();
            Log.d("Position", String.valueOf(position));
        }
    }

    public void goBack(View view) {
        onBackPressed();
    }
}
