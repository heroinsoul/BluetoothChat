package com.example.android.BluetoothChat;

import java.util.Date;

/**
 * Created by holod on 1/26/16.
 */
public class ForwardList {
    public String deviceMac;
    public String beaconId;
    public Date time;
    public Integer messageKey;

    public ForwardList(String deviceMac, String beaconId, Date time, Integer messageKey) {
        this.deviceMac = deviceMac;
        this.beaconId = beaconId;
        this.time = time;
        this.messageKey = messageKey;
    }
};



