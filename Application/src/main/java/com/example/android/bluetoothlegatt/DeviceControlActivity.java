/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private TextView mConnectionState;
    private String mDeviceName;
    private String mDeviceAddress;

    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    
    private TextView lblMotion;
    private TextView lblRelay;
    private Button btnGoForward;
    private Button btnGoBack;
    private Button btnRotateLeft;
    private Button btnRotateRight;
    private Button btnStop;
    private ToggleButton tgbRelay1;
    private ToggleButton tgbRelay2;
    private ToggleButton tgbRelay3;
    private ToggleButton tgbRelay4;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                enableControl(false);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                enableControl(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                enableControl(true);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void enableControl(boolean en) {

        lblMotion.setEnabled(en);
        lblRelay.setEnabled(en);
        btnGoForward.setEnabled(en);
        btnGoBack.setEnabled(en);
        btnRotateLeft.setEnabled(en);
        btnRotateRight.setEnabled(en);
        btnStop.setEnabled(en);
        tgbRelay1.setEnabled(en);
        tgbRelay2.setEnabled(en);
        tgbRelay3.setEnabled(en);
        tgbRelay4.setEnabled(en);

        if(en == false) {
            tgbRelay1.setChecked(false);
            tgbRelay2.setChecked(false);
            tgbRelay3.setChecked(false);
            tgbRelay4.setChecked(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bot_action);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //control
        lblMotion = (TextView) findViewById(R.id.lblMotion);
        lblRelay = (TextView) findViewById(R.id.lblRelay);
        btnGoForward = (Button) findViewById(R.id.btnGoForward);
        btnGoBack = (Button) findViewById(R.id.btnGoBack);
        btnRotateLeft = (Button) findViewById(R.id.btnRotateLeft);
        btnRotateRight = (Button) findViewById(R.id.btnRotateRight);
        btnStop = (Button) findViewById(R.id.btnStop);
        tgbRelay1 = (ToggleButton) findViewById(R.id.tgbRelay1);
        tgbRelay2 = (ToggleButton) findViewById(R.id.tgbRelay2);
        tgbRelay3 = (ToggleButton) findViewById(R.id.tgbRelay3);
        tgbRelay4 = (ToggleButton) findViewById(R.id.tgbRelay4);
        enableControl(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, BotGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, BotGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /*
     *  @brief      Hành động khi nhấn nút Forward
     */
    public void onClickForward(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_motion_char_uuid,
                    BotGattAttributes.FORWARD);

            Log.v(TAG, "Go Forward");
        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Back
     */
    public void onClickBack(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_motion_char_uuid,
                    BotGattAttributes.BACK);

            Log.v(TAG, "Go Back");
        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Left
     */
    public void onClickLeft(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_motion_char_uuid,
                    BotGattAttributes.LEFT);

            Log.v(TAG, "Rotate Left");
        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Right
     */
    public void onClickRight(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_motion_char_uuid,
                    BotGattAttributes.RIGHT);

            Log.v(TAG, "Rotate Right");
        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Stop
     */
    public void onClickStop(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_motion_char_uuid,
                    BotGattAttributes.STOP);

            Log.v(TAG, "Stop");
        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Relay 1
     */
    public void onClickRelay1(View v){
        if(mBluetoothLeService != null) {
            int value = BotGattAttributes.RELAY_OFF;
            if (tgbRelay1.isChecked()) {
                value = BotGattAttributes.RELAY_ON;
            }
            else {
                value = BotGattAttributes.RELAY_OFF;
            }

            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_relay1_char_uuid,
                    value);

            Log.v(TAG, "Turn relay 1 " + tgbRelay1.getText());

        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Relay 2
     */
    public void onClickRelay2(View v){
        if(mBluetoothLeService != null) {
            int value = BotGattAttributes.RELAY_OFF;
            if (tgbRelay2.isChecked()) {
                value = BotGattAttributes.RELAY_ON;
            }
            else {
                value = BotGattAttributes.RELAY_OFF;
            }

            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_relay2_char_uuid,
                    value);

            Log.v(TAG, "Turn relay 2 " + tgbRelay2.getText());

        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Relay 3
     */
    public void onClickRelay3(View v){
        if(mBluetoothLeService != null) {
            int value = BotGattAttributes.RELAY_OFF;
            if (tgbRelay3.isChecked()) {
                value = BotGattAttributes.RELAY_ON;
            }
            else {
                value = BotGattAttributes.RELAY_OFF;
            }

            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_relay3_char_uuid,
                    value);

            Log.v(TAG, "Turn relay 3 " + tgbRelay3.getText());

        }
    }

    /*
     *  @brief      Hành động khi nhấn nút Relay 4
     */
    public void onClickRelay4(View v){
        if(mBluetoothLeService != null) {
            int value = BotGattAttributes.RELAY_OFF;
            if (tgbRelay4.isChecked()) {
                value = BotGattAttributes.RELAY_ON;
            }
            else {
                value = BotGattAttributes.RELAY_OFF;
            }

            mBluetoothLeService.writeCustomCharacteristic(
                    BotGattAttributes.bot_service_uuid,
                    BotGattAttributes.bot_relay4_char_uuid,
                    value);

            Log.v(TAG, "Turn relay 4 " + tgbRelay4.getText());

        }
    }
}
