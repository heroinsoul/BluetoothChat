package com.example.android.BluetoothChat;

import java.util.ArrayList;

/**
 * Created by holod on 12/10/15.
 */
public class Message {

    private int id;
    private String text;
    private String destination;
    private ArrayList<String> beacons;

    public Message(int id, String text, String destination) {
        this.id = id;

        beacons = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    //Complete getters

    public ArrayList<String> getBeacons() {
        return beacons;
    }





    public void setId(int id) {
        this.id = id;
    }

    //Complete Setters

    public void setBeacons(ArrayList<String> beacons) {
        this.beacons = beacons;
    }



    public void addBeacon(String beacon) {
        beacons.add(beacon);
    }

    public void removeBeacon(String beacon) {
        beacons.remove(beacon);
    }




}
