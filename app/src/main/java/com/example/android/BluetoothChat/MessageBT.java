package com.example.android.BluetoothChat;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by holod on 12/10/15.
 */
public class MessageBT implements Serializable{

    private int id;
    private String text;
    private String destination;
    private String beaconId;
    private int sprayCount;
    //    private ArrayList<String> beacons;
    private static int messageCount;


    public MessageBT(String text, String destination, String beaconId) {
//        this.id = messageCount++;
        Random r = new Random();
        int randNum = r.nextInt(10000 - 1 + 1) + 1;
        this.id = randNum;
        this.text = text;
        this.destination = destination;
        this.beaconId = beaconId;
        int spraycount = 0;
        this.sprayCount = spraycount;
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

    public int getSprayCount() {
        return sprayCount;
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

    public void setSprayCount(int sprayCount) {
        this.sprayCount = sprayCount;
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
