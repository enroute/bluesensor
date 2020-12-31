package com.ztfun.bluesensor.model;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ztfun.bluesensor.BlueSensorApplication;
import com.ztfun.bluesensor.events.CharacteristicChangedEvent;
import com.ztfun.bluesensor.events.CharacteristicReadEvent;
import com.ztfun.bluesensor.events.ConnectEvent;
import com.ztfun.bluesensor.events.ConnectionStateChangeEvent;
import com.ztfun.bluesensor.events.GattServiceDiscoveredEvent;
import com.ztfun.bluesensor.events.ReadRemoteRssiEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BlueBleService extends Service {
    private static final String TAG = BlueBleService.class.getSimpleName();

    private BleEngine bleEngine;

    public BlueBleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        ((BlueSensorApplication)getApplication()).setBleService(this);
        bleEngine = ((BlueSensorApplication)getApplication()).getBleEngine();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        ((BlueSensorApplication)getApplication()).setBleService(null);
        super.onDestroy();
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(ConnectEvent event) {
        bleEngine.connect(event.address);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(ConnectionStateChangeEvent event) {
        Log.d(TAG, "onMessageEvent(ConnectionStateChangeEvent)");
        Log.d(TAG, event.getStateString());
    }
}
