package com.ztfun.bluesensor.events;

import android.bluetooth.BluetoothProfile;

public class ConnectionStateChangeEvent {
    public final int state; // BluetoothProfile.STATE_CONNECTED
    public ConnectionStateChangeEvent(int state) {
        this.state = state;
    }

    public String getStateString() {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "BluetoothProfile.STATE_CONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "BluetoothProfile.STATE_CONNECTING";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "BluetoothProfile.STATE_DISCONNECTING";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "BluetoothProfile.STATE_DISCONNECTED";
            default:
                return "INVALID STATE";
        }
    }
}
