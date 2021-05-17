package com.ztfun.bluesensor.model;

import android.content.Context;

public class SmartBleDevice extends BaseBleDevice {
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public SmartBleDevice(Context context, String address) {
        super(context, address);
    }

    @Override
    protected void setCharacteristics() {
        super.setCharacteristics();


    }
}
