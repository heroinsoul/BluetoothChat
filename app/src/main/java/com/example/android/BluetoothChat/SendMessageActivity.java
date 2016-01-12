package com.example.android.BluetoothChat;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SendMessageActivity extends Activity {

    // Debugging
    private static final String TAG = "DEBUG";
    private static final boolean D = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_beacon);

        // Spinner element
        final Spinner beaconsSpinner = (Spinner) findViewById(R.id.beaconsSpinner);

        // Spinnet onclick listener
        beaconsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<String> beaconList = new ArrayList<String>();
        beaconList.addAll(BluetoothChat.allBeacons.keySet());

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, beaconList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        beaconsSpinner.setAdapter(dataAdapter);


        final EditText textEditMAC = (EditText)findViewById(R.id.destID);
        final EditText textEditMSG = (EditText)findViewById(R.id.msgText);

        Button sendMessage = (Button) findViewById(R.id.sendButton);
        sendMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String destinationID = textEditMAC.getText().toString();
                String message = textEditMSG.getText().toString();
                String beaconKey = beaconsSpinner.getSelectedItem().toString();
                String beaconValue = "";

                // Get value for the given key out of the allBeacons hashmap
                Iterator it = BluetoothChat.allBeacons.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry beacon = (Map.Entry) it.next();
                    if (beacon.getKey().equals(beaconKey)) {
                        beaconValue = beacon.getValue().toString();
                    }
                }
                Log.d(TAG,"Recorded values: " + destinationID + " " + message + " " + beaconValue);

                Message msg = new Message(message, destinationID, beaconValue);
                BluetoothChat.messageHashMap.put(msg.getId(),msg);


            }
        });

        Button cancelMessage = (Button) findViewById(R.id.cancelButton);
        cancelMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
