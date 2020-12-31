package com.ztfun.bluesensor.events;

public class ReadRemoteRssiEvent {
    private final int rssi;
    public ReadRemoteRssiEvent(int rssi) {
        this.rssi = rssi;
    }
}
