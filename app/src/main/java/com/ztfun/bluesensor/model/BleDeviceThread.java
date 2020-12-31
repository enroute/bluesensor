package com.ztfun.bluesensor.model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;
import java.util.logging.Handler;

public class BleDeviceThread extends Thread {
    private final static String TAG = BleDeviceThread.class.getSimpleName();

    private BluetoothDevice device;
    private int state;
    private Handler handler;
    private Context context;

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTION_LOST = 3;

    public BleDeviceThread(BluetoothDevice device, Handler handler, Context context) {
        this.device = device;
        this.handler = handler;
        this.context = context;
        state = STATE_IDLE;
    }

    @Override
    public void run() {
        startConnect(true);
        // wait for connection
    }

    private void startConnect(boolean autoConnect) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        // move to activity, since only startActivityForResult can't be called within service
        /**
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
         */

        // BluetoothGatt bluetoothGatt = device.connectGatt(this, autoConnect, gattCallback);
    }

    public void stopListen() {

    }

    public void write(byte[] data) {
        // todo
    }
}
