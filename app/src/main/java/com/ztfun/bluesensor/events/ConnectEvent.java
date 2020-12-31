package com.ztfun.bluesensor.events;

import android.bluetooth.BluetoothDevice;

public class ConnectEvent {
    public final String address;

    public ConnectEvent(String address) {
        this.address = address;
    }
}
