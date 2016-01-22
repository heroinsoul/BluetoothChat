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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private boolean isWriter = false;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }



    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}



        // Start the thread to listen on a BluetoothServerSocket
//        if (mSecureAcceptThread == null) {
//            mSecureAcceptThread = new AcceptThread(true);
//            mSecureAcceptThread.start();
//        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }

        setState(STATE_LISTEN);
    }

//    /**
//     * Start the ConnectThread to initiate a connection to a remote device.
//     * @param device  The BluetoothDevice to connect
//     * @param secure Socket Security type - Secure (true) , Insecure (false)
//     */
//    public synchronized void connect(BluetoothDevice device, boolean secure) {
//        if (D) Log.d(TAG, "connect to: " + device);
//
//        // Cancel any thread attempting to make a connection
//        if (mState == STATE_CONNECTING) {
//            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
//        }
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
//
//        // Start the thread to connect with the given device
//        mConnectThread = new ConnectThread(device, secure);
//        mConnectThread.start();
//        setState(STATE_CONNECTING);
//    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
    //* @param deviceList  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
//    public synchronized void connect(ArrayList<BluetoothDevice> deviceList, boolean secure) {
//
//        for (BluetoothDevice device : deviceList) {
//            if (D) Log.d(TAG, "connect to: " + device);
//
//            // Cancel any thread attempting to make a connection
//            if (mState == STATE_CONNECTING) {
//                if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
//            }
//
//            // Cancel any thread currently running a connection
//            if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
//
//            // Start the thread to connect with the given device
//            mConnectThread = new ConnectThread(device, secure);
//            mConnectThread.start();
//            setState(STATE_CONNECTING);
//
//            break;
//            //while(mState != STATE_LISTEN);
//        }
//
//    }


//    public synchronized void connect(String deviceListString, boolean secure) {
    public void connect(String deviceListString, boolean secure) {

        String []deviceList = deviceListString.split(",");

//        isWriter = true;

        for (String address : deviceList) {
            // Get the BluetoothDevice object

            synchronized (BluetoothChatService.this) {
                BluetoothDevice device = mAdapter.getRemoteDevice(address);

                if (D) Log.d(TAG, "connect to: " + device);

                // Cancel any thread attempting to make a connection
                if (mState == STATE_CONNECTING) {
                    if (mConnectThread != null) {
                        mConnectThread.cancel();
                        mConnectThread = null;
                    }
                }

                // Cancel any thread currently running a connection
                if (mConnectedThread != null) {
                    mConnectedThread.cancel();
                    mConnectedThread = null;
                }

                // Start the thread to connect with the given device
                mConnectThread = new ConnectThread(device, secure);
                mConnectThread.start();
                setState(STATE_CONNECTING);
            }

//            break;
            while(mState != STATE_LISTEN);
        }

    }





    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);


        Log.d(TAG, "before cancelling threads");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        Log.d(TAG, "before cancelling running threads");
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        Log.d(TAG, "before cancelling accepting threads");
        // Cancel the accept thread because we only want to connect to one device
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        Log.d(TAG, "before starting connected threads");
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message_beacon back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message_beacon back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    // Get the BluetoothSocket input and output streams
//                    try {
//                        OutputStream tmpOut = socket.getOutputStream();
//                        tmpOut.write("Hi".getBytes());
//                    } catch (IOException e) {
//                        Log.e(TAG, "temp sockets not created", e);
//                    }
//                    BluetoothChatService.this.start();

                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
//                if (secure) {
//                    tmp = device.createRfcommSocketToServiceRecord(
//                            MY_UUID_SECURE);
//                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                    isWriter = true;
//                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            Log.d(TAG, "After cancel Discovery");

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
//                Log.d(TAG, "Before socket connect");
                mmSocket.connect();
//                Log.d(TAG, "After socket connect");
            } catch (IOException e) {
                // Close the socket
//                Log.d(TAG, "EXCEPTION WHILE CONNECTING");

                try {
//                    Log.d(TAG, "Before socket close");
                    mmSocket.close();
//                    Log.d(TAG, "After socket close");
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
//                Log.d(TAG, "Exiting connectThread run()");
                return;
            }

//            Log.d(TAG, "before nullifying mconnectThread");

//            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                Log.d(TAG, "insidesync");
                mConnectThread = null;
            }

            Log.d(TAG, "Calling Service's connected method");
            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);


//            // Reset the ConnectThread because we're done
//            synchronized (BluetoothChatService.this) {
//                Log.d(TAG, "insidesync");
//                mConnectThread = null;
//            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

//        public void run() {
//            Log.i(TAG, "BEGIN mConnectedThread");
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//            // Keep listening to the InputStream while connected
//            while (true) {
//                try {
//                    // Read from the InputStream
//                    bytes = mmInStream.read(buffer);
//
//                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "disconnected", e);
//                    connectionLost();
//                    // Start the service over to restart listening mode
//                    BluetoothChatService.this.start();
//                    break;
//                }
//            }
//        }


        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            String receivedMsg = null;

            if(isWriter) {
                Log.e(TAG, "SENDING MESSAGE");
//                write("GET-BEACONS\ne03bce2a92a9\n31141ec342f7\ne59321c47f27\n".getBytes());

                // Send my list of detected beacons while requesting the same from other client
                String myDetectedBeacons = "GET-BEACONS\n";
                Iterator it = BluetoothChat.beaconMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry beacon = (Map.Entry) it.next();
                    myDetectedBeacons += beacon.getKey() + "\n";
                }
                write(myDetectedBeacons.getBytes());
                isWriter = false;
            }

            // Keep listening to the InputStream while connected
            while (true) {
                try {

                    Log.d(TAG, "Reading from input...");
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);


                    receivedMsg = new String(buffer);

//                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();

                    //READER
                    if(receivedMsg.contains("GET-BEACONS")) {

                        Log.d(TAG, " -----#########----- GOT A BEACON REQUEST!");

                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();

                        // Split received beacon list by lines
                        String[] separated = receivedMsg.split("\n");

                        ArrayList<String> receivedBeacons = new ArrayList<>();

                        // Iterate through the list of received beacons
                        // and remove the bytes garbage at the last line
                        // as well as ignore the GET_BEACONS first line

                        // Add them to the receivedBeacons array list
                        for (int s=1; s<separated.length-1; s++) {
                            receivedBeacons.add(separated[s]);
                            Log.d(TAG, "-----#########----- GET-BEACON: " + separated[s]);
                        }


                        // Reply with my list of detected beacons
                        String readerBeacons = "BEACONS-REPLY\n";
                        Iterator it = BluetoothChat.beaconMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry beacon = (Map.Entry) it.next();
                            readerBeacons += beacon.getKey() + "\n";
                        }
                        write(readerBeacons.getBytes());

//                        // Check if we have a message to forward to this device
//                        ArrayList<MessageBT> messageList = compareBeaconsMessages(receivedBeacons);
//                        if (!messageList.isEmpty()) {
//                            // forward the messages we have for any matched beacon
//                            try{
//                                // Serialize data object to a byte array
//                                ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
//                                ObjectOutputStream out = new ObjectOutputStream(bos) ;
//                                out.writeObject(messageList);
//                                out.close();
//
//                                // Get the bytes of the serialized object
//                                byte[] messagebuf = bos.toByteArray();
//
//                                String messageForwarded = "MSG\n";
//
//                                byte [] combined = new byte[messagebuf.length + messageForwarded.getBytes().length +1];
//                                System.arraycopy(messageForwarded.getBytes(), 0, combined, 0, messageForwarded.getBytes().length);
//                                System.arraycopy(messagebuf, 0, combined, messageForwarded.getBytes().length, messagebuf.length);
//                                System.arraycopy("\n".getBytes(), 0, combined, messageForwarded.getBytes().length + messagebuf.length, 1);
//                                write(combined);
////                                String combinedString = new String(combined);
////                                if (combinedString.charAt(combinedString.length()-1) == '\n') {
////                                    Log.d(TAG, "#############  This is the message we send: " + combinedString.split("\n").length);
////                                }
////                                else {
////                                    Log.d(TAG, " @@@@@@ DIDNT WORK");
////                                }
//                            } catch (IOException e) {
//                            }
//
//                        }

                        // Start the service over to restart listening mode
//                        BluetoothChatService.this.start();
//                        break;
                    }


                    //WRITER
                    if(receivedMsg.contains("BEACONS-REPLY")){

                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();

                        // Process list of clients beacons
                        // Split received beacon list by lines
                        String[] separated = receivedMsg.split("\n");

                        ArrayList<String> receivedBeacons = new ArrayList<>();

                        // Iterate through the list of received beacons
                        // and remove the bytes garbage at the last line
                        // as well as ignore the GET_BEACONS first line

                        // Add them to the receivedBeacons array list
                        for (int s=1; s<separated.length-1; s++) {
                            receivedBeacons.add(separated[s]);
                        }

                        // Check if we have a message to forward to this device
                        ArrayList<MessageBT> messageList = compareBeaconsMessages(receivedBeacons);
                        if (!messageList.isEmpty()) {
                            // forward the messages we have for any matched beacon
                            try {
                                // Serialize data object to a byte array
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(bos);
                                out.writeObject(messageList);
                                out.close();

                                // Get the bytes of the serialized object
                                byte[] messagebuf = bos.toByteArray();

                                String messageForwarded = "MSG\n";

                                byte[] combined = new byte[messagebuf.length + messageForwarded.getBytes().length + 1];
                                System.arraycopy(messageForwarded.getBytes(), 0, combined, 0, messageForwarded.getBytes().length);
                                System.arraycopy(messagebuf, 0, combined, messageForwarded.getBytes().length, messagebuf.length);
                                System.arraycopy("\n".getBytes(), 0, combined, messageForwarded.getBytes().length + messagebuf.length, 1);
                                write(combined);
//                                String combinedString = new String(combined);
//                                if (combinedString.charAt(combinedString.length()-1) == '\n') {
//                                    Log.d(TAG, "#############  This is the message we send: " + combinedString.split("\n").length);
//                                }
//                                else {
//                                    Log.d(TAG, " @@@@@@ DIDNT WORK");
//                                }

                                Log.d(TAG, " ------------------ BEFORE UPDATING THE SPRAY COUNT ------------");
                                // Update the spraycount of messages we just forwarded
                                for (MessageBT msg: messageList) {
                                    BluetoothChat.messageHashMap.get(msg.getId()).setSprayCount(1);
                                }

                                Log.d(TAG, " ------------------ AFTER UPDATING THE SPRAY COUNT ------------");

                            } catch (IOException e) {
                            }
                        }

//                        String disconnectMessage = "DISCONNECT\n";
//                        write(disconnectMessage.getBytes());
                        BluetoothChatService.this.start();
                        break;
                    }
//                        BluetoothChatService.this.start();
//                        break;


                    // Process incoming message
                    if (receivedMsg.contains("MSG")) {

                        Log.d(TAG, " -----#########----- RECEIVED MSG MESSAGE");
                        // Check if message is for us
                        // ...
                        // if yes display it
                        mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
//                        // if not, store the message in hashmap
//                        // Start from splitting the message
                        String[] msgLines = receivedMsg.split("\n");
//
//                        // Add the message into the existing MessageBT hashmap
                        for (int s=1; s<msgLines.length-1; s++) {
//                            BluetoothChat.messageHashMap.put(MessageBT.getId(),msgLines[s])
                            Log.d(TAG, "THIS IS THE MESSAGE RECEIVED = " + msgLines[s]);
                        }

                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        byte[] combined = new byte[msgLines[1].length()];
                        System.arraycopy(buffer, 4, combined, 0, msgLines[1].length());

                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(combined));
                        try {
                            @SuppressWarnings("unchecked")
                            ArrayList<MessageBT> list = (ArrayList<MessageBT>) ois.readObject();
                            ois.close();
                            for (MessageBT messageBT: list) {
                                Log.d(TAG, " Message content: " + "ID " + messageBT.getId() + " " + "Dest" +" " + messageBT.getDestination() + "Text" + " " + messageBT.getText() + " " + "Beacon ID" + " " + messageBT.getBeaconId());
                                 // Check if the message is for us
                                // Get local Bluetooth adapter
//                                if (messageBT.getDestination().equals(mBluetoothAdapter.getAddress())) {
//                                    // if yes display the message
//                                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                                            .sendToTarget();
//                                };
                                // Check if we already have the message with the same ID
                                if (!BluetoothChat.messageHashMap.containsKey(messageBT.getId())) {
                                    Log.d(TAG, " --------------- MESSAGE IS NEW ---------------");
//                                    MessageBT msg = new MessageBT(messageBT.getText(), messageBT.getDestination(), messageBT.getBeaconId());
//                                    BluetoothChat.messageHashMap.put(messageBT.getId(), msg);
                                    BluetoothChat.messageHashMap.put(messageBT.getId(), messageBT);
                                    // Set spray count=1 for direct delivery to destination
                                    // Since message was forwarded to us already
                                    BluetoothChat.messageHashMap.get(messageBT.getId()).setSprayCount(1);
                                }
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                        }

//                        // Send the obtained bytes to the UI Activity
//                        mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                                .sendToTarget();


                        // Check if we have a MSG to send or if we have already done that
                        // if yes send it
                        // if no send DISCONNECT msg
//                        BluetoothChatService.this.start();
//                        break;
//                        String disconnectMessage = "DISCONNECT\n";
//                        write(disconnectMessage.getBytes());
                        BluetoothChatService.this.start();
                        break;
                    }


//                    // Disconnect the active thread only if DISCONNECT message received
//                    if (receivedMsg.contains("DISCONNECT")) {
//                        BluetoothChatService.this.start();
//                        break;
//                    }

//                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();


                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothChatService.this.start();
                    break;
                }
            }

//            isWriter = false;
        }


        public ArrayList<MessageBT> compareBeaconsMessages(ArrayList <String> beaconslist) {
            ArrayList<MessageBT> messageList = new ArrayList<>();
            for (Map.Entry<Integer, MessageBT> msg : BluetoothChat.messageHashMap.entrySet()) {
                // Look for messages that are intended for any of the received beacons
                // Consider number of times the messages has been already forwarded (max 1 for now)
                if ((beaconslist.contains(msg.getValue().getBeaconId())) && (msg.getValue().getSprayCount()<1)) {
                    Log.d(TAG, " -------- THIS IS THE MESSAGE I SEND: " + "ID: " + msg.getValue().getId() + "\n" + "Value: " + msg.getValue());
                    messageList.add(msg.getValue());
                }
            }
            return messageList;
        }


        /**
         *  to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message_beacon back to the UI Activity
                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
