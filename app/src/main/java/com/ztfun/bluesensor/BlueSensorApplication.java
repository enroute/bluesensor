package com.ztfun.bluesensor;

import android.app.Application;
import android.content.Intent;

import com.ztfun.bluesensor.model.BleEngine;
import com.ztfun.bluesensor.model.BlueBleService;

public class BlueSensorApplication extends Application {
    private BleEngine bleEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        bleEngine = new BleEngine(this);
    }

    public BleEngine getBleEngine() {
        if (bleEngine == null) {
            bleEngine = new BleEngine(this);
        }

        return bleEngine;
    }

    private BlueBleService bleService = null;
    public void startBleService() {
        if (bleService == null) {
            startService(new Intent(getApplicationContext(), BlueBleService.class));
        }
    }

    public void setBleService(BlueBleService bleService) {
        this.bleService = bleService;
    }
}
