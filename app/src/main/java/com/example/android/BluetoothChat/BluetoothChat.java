/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.BluetoothChat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    private boolean messageReady = false;

    // MessageBT types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    // List of detected beacons
    public static HashMap<String, String> beaconMap = new HashMap<>();

    // Incoming and outgoing messages list
    public static HashMap<Integer, MessageBT> messageHashMap = new HashMap<>();

    // List of all the beacons deployed out there. Not to confuse with
    // beaconMap, which is the list of detected beacons.
    public static HashMap<String, String> allBeacons = new HashMap<String, String>() {
        {
            put("1st floor","EC:74:61:FE:EC:26");
            put("2nd floor","F9:7C:2B:0F:3D:A7");
            put("3rd floor","E6:BF:80:AE:AF:63");
            put("4th floor","F3:CE:E0:F8:6B:E2");
            put("5th floor","DF:8B:3E:EE:C6:1C");
        };
    };

    // Create an array of devices I'm connecting to
    public static ArrayList<ForwardList> forwardListArray = new ArrayList<>();

    private Button discoverButton;

    public int randomInteger(int min, int max) {

        Random rand = new Random();

        // nextInt excludes the top value so we have to add 1 to include the top value
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ensureDiscoverable();



        Log.d("This is my MAC   " + mBluetoothAdapter.getAddress(),TAG);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }




        // Snippet to run the discovery process every 20 seconds
        // considering it only takes 12 sec to finish the discovery
        final Handler deviceDiscoveryHandler = new Handler();
        deviceDiscoveryHandler.post(new Runnable() {
            @Override
            public void run() {
                toggleDiscovery();
                deviceDiscoveryHandler.postDelayed(this, 20000);
            }
        });

//        final Handler deviceConnectionHandler = new Handler();
//        deviceConnectionHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                processBTChatlist();
//                deviceConnectionHandler.postDelayed(this,15000);
//            }
//        });




        Button showBeaconsButton = (Button) findViewById(R.id.showBeaconsButton);
        showBeaconsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mConversationArrayAdapter.clear();
                Iterator it = beaconMap.entrySet().iterator();
                mConversationArrayAdapter.add("My MAC address is " + mBluetoothAdapter.getAddress());
                while (it.hasNext()) {
                    Map.Entry beacon = (Map.Entry) it.next();
                    Log.d(TAG, "THE LIST CONTAINS: " + beacon.getKey() + " - " + beacon.getValue());
                    if (beacon.getValue().equals("iBKS105")) {
                        mConversationArrayAdapter.add(beacon.getKey() + " - " + beacon.getValue());
                    }
                }
            }
        });

        // Button to create and send a message to a specific ID

        Button sendBeaconMsg = (Button) findViewById(R.id.sendMessageButton);
        sendBeaconMsg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), SendMessageActivity.class);
                startActivity(myIntent);
            }
        });

        discoverButton = (Button) findViewById(R.id.discover_button);
        discoverButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDiscovery();
            }
        });
    }


    /**
     * Start device discover with the BluetoothAdapter
     */
    private void toggleDiscovery() {
        if (D) Log.d(TAG, "toggleDiscovery()");

//        // If we're already discovering, stop it
//        if (mBluetoothAdapter.isDiscovering()) {
//            discoverButton.setText("Start Discovery");
//            mBluetoothAdapter.cancelDiscovery();
//        }
//        else {
//            // Request discover from BluetoothAdapter
//            discoverButton.setText("Stop Discovery");
//            mBluetoothAdapter.startDiscovery();
//        }
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }


    List<String> btChatClientsList = new ArrayList<String>();

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))  {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
//                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getName().equals("iBKS105"))) {
                if ((device.getName() != null) && (device.getName().equals("iBKS105"))) {
                    beaconMap.put(device.getAddress(), device.getName());
                }

                // If the discovered device is BTChat client, add its MAC address to the list of discovered clients
//                if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && (device.getName().equals("BTChat"))) {
                if ((device.getName() != null) && (device.getName().equals("BTChat"))) {
                    Log.d(TAG, "Device detected: " + device.getName() + " " + device.getAddress());
//                    synchronized (BluetoothChat.this) {
//                        // Create the result Intent and include the MAC address
//                        Intent deviceIntent = new Intent();
//                        deviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());
//                        connectDevice(deviceIntent, false);
//                    }
                    if (!btChatClientsList.contains(device.getAddress())) {
                        btChatClientsList.add(device.getAddress());
                    }

                }
            }

            // When the discovery is finished process the list of discovered BTChat clients
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, " ------------- DISCOVERY HAS FINISHED --------------------");
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show();
                processBTChatlist();
            }

//                setProgressBarIndeterminateVisibility(false);
//                setTitle(R.string.select_device);
//                if (mNewDevicesArrayAdapter.getCount() == 0) {
//                    String noDevices = getResources().getText(R.string.none_found).toString();
//                    mNewDevicesArrayAdapter.add(noDevices);
//                }

        }
    };

    // Process the list of detected BTChat devices after the each discovery
    //
    private void processBTChatlist() {
//        mConversationArrayAdapter.clear();
        int listSize = btChatClientsList.size();
        // Check if other clients were detected
        // If yes, check if we have messages to forward
        if ((!btChatClientsList.isEmpty()) && (!messageHashMap.isEmpty())){
            // Check if we have messages with SprayCount = 0
            for (Integer key : messageHashMap.keySet()) {
                if (messageHashMap.get(key).getSprayCount() == 0) {
                    messageReady = true;
                    break; // at least one is enough
                }
            }

            if (messageReady) {
                // If all good initiate connection to a device(-s)

                // Cancel discovery because it's costly and we're about to connect
//                mBluetoothAdapter.cancelDiscovery();
                for (int i=0; i < listSize; i++) {
                    Log.d(TAG, " ------------ This is a device I'm going to connect to: " + btChatClientsList.get(i));
                    Intent deviceIntent = new Intent();
                    deviceIntent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, btChatClientsList.get(i));
                    // Set result
                    setResult(Activity.RESULT_OK, deviceIntent);
                    connectDevice(deviceIntent, false);
                }
                btChatClientsList.clear();
                messageReady = false;
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "No other clients found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message_beacon using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);

        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            // Make the device always discoverable by setting duration to 0
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);

            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message_beacon.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message_beacon bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message_beacon
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    private final void setStatus(int resId) {
//        final ActionBar actionBar = getActionBar();
//        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
//        final ActionBar actionBar = getActionBar();
//        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
//        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        String []deviceList = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS).split(",");
        ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
        for (String address : deviceList) {
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            btDeviceList.add(device);
        }

        // Attempt to connect to the device
//        mChatService.connect(btDeviceList, secure);
        Log.d(TAG, "BEFORE CONNECTING TO THE DEVICE");
        mChatService.connect(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS), secure);
        Log.d(TAG, "AFTER CONNECTING TO THE DEVICE");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
//            // Launch the DeviceListActivity to see devices and do scan
//            serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
//            for (Map.Entry<Integer, MessageBT> msg : BluetoothChat.messageHashMap.entrySet()) {
//                mConversationArrayAdapter.add("Here is a MessageHashMap I have\n" + msg.getValue());
//            }

                for (Integer name: messageHashMap.keySet()) {
                    String key = name.toString();
                    String value = messageHashMap.get(name).toString();
                    mConversationArrayAdapter.add("Here is a MessageHashMap I have\n" + "Key: " + key +
                            "\n" + "Spray count: " + messageHashMap.get(name).getSprayCount() +
                            "\n" + "Value: " + value);
                }
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }



}
