package com.ztfun.bluesensor.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceManager {
    private Context context;
    public BleDeviceManager(Context context) {
        this.context = context;
    }

    private List<BaseBleDevice> deviceList = new ArrayList<>();

    public void connect(String address) {
        BaseBleDevice device = getDevice(address);
        if (device == null) {
            device = new BaseBleDevice(context, address);
            deviceList.add(device);
        }

        device.connect();
    }

    private BaseBleDevice getDevice(String address) {
        for (BaseBleDevice device : deviceList) {
            if (device.getMac().equals(address)) {
                return device;
            }
        }
        return null;
    }

    public void disconnect(String address) {
        BaseBleDevice device = getDevice(address);
        if (device != null) {
            device.disconnect();
        }
    }

    public void disconnectAll() {
        for (BaseBleDevice device : deviceList) {
            device.disconnect();
        }
    }

    public void removeDevice(String address) {
        BaseBleDevice device = getDevice(address);
        if (device != null) {
            device.disconnect();
            deviceList.remove(device);
        }
    }
}
