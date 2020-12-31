package com.ztfun.bluesensor.events;

public class GattServiceDiscoveredEvent {
    public final int status;
    public GattServiceDiscoveredEvent(int status) {
        this.status = status;
    }
}
