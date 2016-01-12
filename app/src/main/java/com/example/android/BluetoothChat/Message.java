package com.example.android.BluetoothChat;

import android.os.Bundle;
import android.util.Log;


import java.util.ArrayList;

/**
 * Created by holod on 12/10/15.
 */
public class Message {

    private int id;
    private String text;
    private String destination;
    private String beaconId;
//    private ArrayList<String> beacons;
    private static int messageCount;


    public Message(String text, String destination, String beaconId) {
        this.id = messageCount++;
        this.text = text;
        this.destination = destination;
        this.beaconId = beaconId;
//        beacons = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    //Complete getters

//    public ArrayList<String> getBeacons() {
//        return beacons;
//    }

    public String getText() {
        return text;
    }

    public String getDestination() {
        return destination;
    }

    public String getBeaconId() {
        return beaconId;
    }




    public void setId(int id) {
        this.id = id;
    }

    //Complete Setters

    public void setText(String text) {
        this.text = text;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

//    public void setBeacons(ArrayList<String> beacons) {
//        this.beacons = beacons;
//    }


//    public void addBeacon(String beacon) {
//        beacons.add(beacon);
//    }

//    public void removeBeacon(String beacon) {
//        beacons.remove(beacon);
//    }




}
