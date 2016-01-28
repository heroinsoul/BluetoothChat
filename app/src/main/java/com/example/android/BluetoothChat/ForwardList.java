package com.example.android.BluetoothChat;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by holod on 1/26/16.
 */
public class ForwardList implements Comparable<ForwardList> {
    public String deviceMac;
    public String beaconId;
    public Date time;
    public ArrayList<Integer> messageKey;

    public ForwardList(String deviceMac, String beaconId, Date time, ArrayList<Integer> messageKey) {
        this.deviceMac = deviceMac;
        this.beaconId = beaconId;
        this.time = time;
        this.messageKey = messageKey;
    }

    @Override
    public int compareTo(ForwardList other) {
        int i = this.beaconId.compareTo(other.beaconId);
        if (i==0) {
            return this.time.compareTo(other.time);
        }
        return i;
    }
};



